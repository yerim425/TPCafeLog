package com.yrlee.tpcafelog.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.FragmentHomeBinding
import com.yrlee.tpcafelog.model.KakaoSearchPlaceResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.yrlee.tpcafelog.data.local.OnHomeItemSelectListener
import com.yrlee.tpcafelog.model.HashTagItem
import com.yrlee.tpcafelog.model.CafeInfoRequest
import com.yrlee.tpcafelog.model.HomeHashtagFilteringRequest
import com.yrlee.tpcafelog.model.HomeHashtagFilteringResponse
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.ui.map.MapCafeActivity
import com.yrlee.tpcafelog.ui.map.MapWebViewActivity
import com.yrlee.tpcafelog.util.Constants
import com.yrlee.tpcafelog.util.LocationUtils
import com.yrlee.tpcafelog.util.PrefUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class HomeFragment : Fragment(), OnHomeItemSelectListener {
    val TAG = "home fragment"

    lateinit var binding: FragmentHomeBinding
    private lateinit var categoryItems: Array<String>
    private lateinit var categoryQuery: String
    private var searchQuery: String = ""
    private var page = 1
    private var totalCnt = 0
    var myLocation: Location? = null // 내 위치
    private var isLoadingGetHomeList = false
    private val userId = PrefUtils.getInt("user_id") // 없으면 -1

    // adapter
    lateinit var categoryAdapter: HomeCategoryAdapter
    lateinit var cafeAdapter: HomeCafeAdapter
    lateinit var hashtagAdapter: HomeHashtagAdapter

    val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it){
            viewLifecycleOwnerLiveData.value?.let{
                it.lifecycleScope.launch {
                    requestSearchCafesFromKakao()
                }
            }
            Toast.makeText(requireContext(), getString(R.string.message_restricted_search_func), Toast.LENGTH_SHORT).show()
        }else {
            LocationUtils.requestMyLocation(requireContext()) {
                it?.let {
                    myLocation = it
                    viewLifecycleOwnerLiveData.value?.let {
                        lifecycleScope.launch {
                            requestSearchCafesFromKakao()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryQuery = getString(R.string.coffee_shop)
        setCategoryList()
        setHashtagList()

        // 내 위치 정보 취득에 대한 동적 퍼미션
        val permissionResult = requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionResult == PackageManager.PERMISSION_DENIED)
            permissionResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) // 퍼미션 요청
        else LocationUtils.requestMyLocation(requireContext()){
            it?.let{
                myLocation = it
                viewLifecycleOwnerLiveData.value?.let{
                    lifecycleScope.launch {
                        requestSearchCafesFromKakao()
                    }
                }
            }
        }

        cafeAdapter = HomeCafeAdapter(requireContext())
        binding.recyclerviewHome.adapter = cafeAdapter

        // 검색 리스너
        binding.edtSearchHome.setOnEditorActionListener { v, actionId, event ->
            searchQuery = binding.edtSearchHome.text.toString().trim()
            if (searchQuery.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.message_input_search_query),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                categoryAdapter.setUnselect()
                page = 1
                totalCnt = 0
                viewLifecycleOwnerLiveData.value?.let{
                    it.lifecycleScope.launch {
                        requestSearchCafesFromKakao()
                    }
                }
                binding.edtSearchHome.setText(searchQuery)
                binding.edtSearchHome.clearFocus()

                hideKeyboard()

            }
            false
        }

        // 리사이클러뷰 스크롤 리스너
        binding.recyclerviewHome.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            hideKeyboard()
            if(isLoadingGetHomeList) return@setOnScrollChangeListener
            // 스크롤 시 화면에 보이는 카페들의 이미지 요청
            val layoutManager = binding.recyclerviewHome.layoutManager as LinearLayoutManager
            val firstVisible = layoutManager.findFirstVisibleItemPosition()
            val lastVisible = layoutManager.findLastVisibleItemPosition()
            val size = lastVisible - firstVisible + 1

            viewLifecycleOwnerLiveData.value?.let {
                lifecycleScope.launch {
                    requestSearchNaverImage(firstVisible, size)
                }
            }

            if (!binding.recyclerviewHome.canScrollVertically(1)) {

                if(cafeAdapter.itemCount == 0) return@setOnScrollChangeListener
                if (cafeAdapter.itemCount < totalCnt) {
                    // 다음 페이지 요청
                    if (binding.progressbar.isGone) { // 중복 요청 방지
                        page++
                        Log.d(TAG, "${cafeAdapter.itemCount} : $totalCnt")
                        viewLifecycleOwnerLiveData.value?.let{
                            it.lifecycleScope.launch {
                                if(hashtagAdapter.getHashtagList().isNotEmpty()){
                                    requestHomeCafeHashtagFiltering()
                                }else{
                                    requestSearchCafesFromKakao()
                                }

                            }
                        }
                    }
                }

            }

            binding.recyclerviewHomeCategory.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                hideKeyboard()
            }
            binding.recyclerviewHomeHashtag.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                hideKeyboard()
            }
        }

        // 지도 화면으로 이동
        binding.btnMap.setOnClickListener {
//            val intent = Intent(requireContext(), MapCafeActivity::class.java)
//            intent.putExtra("latitude", myLocation?.latitude ?: "")
//            intent.putExtra("longitude", myLocation?.longitude ?: "")
//            startActivity(intent)
            startActivity(Intent(requireContext(), MapWebViewActivity::class.java))
        }
    }

    fun setCategoryList() {
        categoryItems = arrayOf(
            getString(R.string.coffee_shop),
            getString(R.string.dessert_cafe),
            getString(R.string.unmanned_cafe),
            getString(R.string.cartoon_cafe),
            getString(R.string.board_cafe),
            getString(R.string.study_cafe),
            getString(R.string.book_cafe),
            getString(R.string.kids_cafe),
            getString(R.string.live_cafe),
            getString(R.string.galley_cafe),
            getString(R.string.dogs_cafe),
            getString(R.string.cats_cafe),
            getString(R.string.fruits_shop),
            getString(R.string.meeting_space)
        )
        categoryAdapter = HomeCategoryAdapter(requireContext(), categoryItems.toList(), this)
        binding.recyclerviewHomeCategory.adapter = categoryAdapter
    }

    fun setHashtagList() {

        val call = RetrofitHelper.getMyService().getHastTagNames(filter = "home")
        call.enqueue(object : Callback<MyResponse<List<HashTagItem>>>{
            override fun onResponse(
                call: Call<MyResponse<List<HashTagItem>>>,
                response: Response<MyResponse<List<HashTagItem>>>
            ) {
                if(response.isSuccessful){
                    val body = response.body()
                    body?.let{
                        it.data?.let{
                            hashtagAdapter = HomeHashtagAdapter(requireContext(), it, this@HomeFragment)
                            binding.recyclerviewHomeHashtag.adapter = hashtagAdapter
                        }
                    }
                }else{
                    Log.e(TAG, response.errorBody()?.string() ?: "errorBody is null")
                }
            }

            override fun onFailure(call: Call<MyResponse<List<HashTagItem>>>, t: Throwable) {
                Log.e(TAG, "${t.message}")
            }

        })
    }

    // 카페 리스트 요청
    fun requestSearchCafesFromKakao() {
        binding.progressbar.visibility = View.VISIBLE

        var query = (searchQuery + " " + categoryAdapter.getSelectedCategory()).trim()
        if(query.isEmpty()) query = getString(R.string.cafe)

        val call = RetrofitHelper.getKakaoService().getSearchCafes(
            query = query,
            page = page,
            longitude = myLocation?.longitude?.toString(),
            latitude = myLocation?.latitude?.toString()
        )
        Log.d(TAG, "${myLocation?.latitude}")
        call.enqueue(object : Callback<KakaoSearchPlaceResponse> {
            override fun onResponse(
                call: Call<KakaoSearchPlaceResponse>,
                response: Response<KakaoSearchPlaceResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d(TAG, "cafe 리스트 요청 성공 ${response.body()?.meta?.total_count}")
                    val body = response.body()
                    body?.let {
                        if (page == 1) {
                            cafeAdapter.clearItemList()
                            totalCnt = it.meta.total_count
                            Log.d(TAG, "total count: $totalCnt")
                            binding.recyclerviewHome.smoothScrollToPosition(0)
                            binding.tvNoCafe.visibility = View.GONE
                        }
                        cafeAdapter.addItems(it.documents)

                        isLoadingGetHomeList = true

                        // 서버에 카페 정보 검색
                        val placeIds = it.documents.map { it.id }

                        viewLifecycleOwnerLiveData.value?.let{
                            it.lifecycleScope.launch {
                                requestHomeCafeList(userId, placeIds)
                            }
                        }
                    }
                } else {
                    Log.d(TAG, response.errorBody()?.string().toString())
                }
            }

            override fun onFailure(call: Call<KakaoSearchPlaceResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "${t.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "${t.message}")
            }
        })
        binding.progressbar.visibility = View.GONE
        binding.edtSearchHome.clearFocus()
    }

    suspend fun requestHomeCafeList(userId: Int, placeIds: List<String>){
        try{
            val request = CafeInfoRequest(userId, placeIds)
            val response = RetrofitHelper.getMyService().getCafeInfos(request)
            val status = response.status
            when(status) {
                200 -> {
                    response.data?.let { data ->
                        cafeAdapter.updateDBdata(data) // DB data로 카페 리스트 업데이트

                        val nullIdx = data.indexOfFirst { it.visit_datas == null }
                        Log.d(TAG, "nullIdx: ${nullIdx}")
                        if(nullIdx > -1 && nullIdx < 5){ // 처음에는 화면에 보이는 리스트만 확인 이미지 검색
                            viewLifecycleOwnerLiveData.value?.let{
                                lifecycleScope.launch {
                                    requestSearchNaverImage(0, data.size)
                                }
                            }
                        }
                    }
                }
                400 -> {
                    Log.d(TAG, "${response.status}: ${response.data}")
                }
            }
        }catch (e:Exception){
            Log.e(TAG, "홈 데이터 요청 실패: ${e.message}")
        }finally {
            isLoadingGetHomeList = false
        }
    }

    // 카테고리 or 해시태그 아이템 클릭 콜백
    override fun onItemSelected(type: Int) {

        hideKeyboard()
        page = 1
        totalCnt = 0

        when(type){
            Constants.CATEGORY_TYPE -> { // 카테고리 클릭 시 카카오 카페 리스트 요청
                viewLifecycleOwnerLiveData.value?.let{
                    lifecycleScope.launch {
                        if(hashtagAdapter.getHashtagList().isNotEmpty()){
                            requestHomeCafeHashtagFiltering() // 해시태그 클릭된 것이 있으면 DB에 있는 카페들을 조회
                        }else{
                            requestSearchCafesFromKakao()
                        }
                    }
                }
            }
            Constants.HASHTAG_TYPE -> { // 해시태그 클릭 시 DB에 해당하는 카페 id 리스트 요청
                viewLifecycleOwnerLiveData.value?.let {
                    lifecycleScope.launch {
                        if(hashtagAdapter.getHashtagList().isNotEmpty()){
                            requestHomeCafeHashtagFiltering() // 해시태그 클릭된 것이 있으면 DB에 있는 카페들을 조회
                        }else{
                            requestSearchCafesFromKakao()
                        }
                    }
                }
            }
        }


    }

    fun requestSearchNaverImage(startIdx: Int, size: Int){
        for (i in startIdx until startIdx+size) {

            val place = cafeAdapter.itemList.getOrNull(i) ?: continue
            Log.d(TAG, "${startIdx}")
            if (!place.isImgRequested && place.visitDatas == null) {
                place.isImgRequested = true
                val address =
                    if (place.address_name.isEmpty()) place.address_name else place.road_address_name
                val city = address.split(" ").firstOrNull() ?: ""
                val query =
                    city + " " + place.place_name + " " + getString(R.string.cafe)
                viewLifecycleOwnerLiveData.value?.let {
                    it.lifecycleScope.launch {
                        val imageUrl = requestCafeImage(query)
                        cafeAdapter.updateImage(i, imageUrl)
                    }
                }
            }
        }
    }

    suspend fun requestCafeImage(query: String): String? {
        return try {
            val response = RetrofitHelper.getNaverService().getSearchImage(query)
            response.items.firstOrNull()?.link
        } catch (e: Exception) {
            Log.e(TAG, "이미지 요청 실패 : ${e.message}")
            null
        }
    }

    fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.edtSearchHome.clearFocus()
        imm.hideSoftInputFromWindow(binding.edtSearchHome.windowToken, 0)

    }

    // 서버에 해시태그에 해당하는 카페 id 리스트 요청
    fun requestHomeCafeHashtagFiltering() {

        var query = binding.edtSearchHome.text.toString().trim()
        var category = categoryAdapter.getSelectedCategory()
        var hashtags = hashtagAdapter.getHashtagList().map { it.no }

        var request = HomeHashtagFilteringRequest(query, category, hashtags, page)

        val call = RetrofitHelper.getMyService().getHomeCafeFiltering(request)
        call.enqueue(object : Callback<MyResponse<HomeHashtagFilteringResponse>> {
            override fun onResponse(
                call: Call<MyResponse<HomeHashtagFilteringResponse>>,
                response: Response<MyResponse<HomeHashtagFilteringResponse>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.data?.let { data->
                        if (page == 1) {
                            cafeAdapter.clearItemList()
                            totalCnt = data.total_cnt
                            binding.recyclerviewHome.smoothScrollToPosition(0)
                            binding.tvNoCafe.visibility = View.GONE
                        }

                        viewLifecycleOwnerLiveData.value?.let{
                            lifecycleScope.launch {

                                val jobs = data.querys.map { query ->
                                    async {
                                        requestSearchCafeFromKakao(query)
                                    }
                                }
                                jobs.awaitAll() // 전부 끝날 때까지 대기

                                val placeIds = cafeAdapter.itemList.map { it.id }
                                Log.d(TAG, "hashtag - placeIds size: ${placeIds.size}")
                                if(placeIds.size > 0){
                                    requestHomeCafeList(userId, placeIds)
                                }else{
                                    binding.tvNoCafe.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                } else {
                    Log.d(TAG, response.errorBody()?.string().toString())
                }
            }

            override fun onFailure(call: Call<MyResponse<HomeHashtagFilteringResponse>>, t: Throwable) {
                Toast.makeText(requireContext(), "${t.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "${t.message}")
            }
        })
        binding.edtSearchHome.clearFocus()
    }

    // 해시태그 필터링된 카페 id로 카페 정보 요청
    suspend fun requestSearchCafeFromKakao(query: String) {

        try{
            val response = RetrofitHelper.getKakaoService().getSearchCafe(
                query = query,
                longitude = myLocation?.longitude?.toString(),
                latitude = myLocation?.latitude?.toString(),
                size = 1
            )
            if(response.meta.total_count == 1) {
                val place = response.documents
                cafeAdapter.addItems(place)
            }else{
                Log.d(TAG, "requestSearchCafeFromKakao response error - $query")
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchCafeFromKakao 요청 실패 : ${e.message}")
        } finally {
            binding.progressbar.visibility = View.GONE
        }

    }
}