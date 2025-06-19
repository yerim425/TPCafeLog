package com.yrlee.tpcafelog.ui.home

import android.Manifest
import android.content.Context
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
import com.yrlee.tpcafelog.model.HomeCafeRequest
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.util.Constants
import com.yrlee.tpcafelog.util.LocationUtils
import com.yrlee.tpcafelog.util.Utils

class HomeFragment : Fragment(), OnHomeItemSelectListener {
    val TAG = "home fragment"

    lateinit var binding: FragmentHomeBinding
    private lateinit var categoryItems: Array<String>
    private lateinit var categoryQuery: String
    private var searchQuery: String = ""
    private var page = 1
    private var totalCnt = 0
    var myLocation: Location? = null // 내 위치
    var isLoadingGetHomeList = false

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
        }else{
            viewLifecycleOwnerLiveData.value?.let{
                it.lifecycleScope.launch {
                    requestSearchCafesFromKakao()
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
                    it.lifecycleScope.launch {
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

            viewLifecycleOwnerLiveData.value?.let{
                lifecycleScope.launch {
                    requestSearchNaverImage(firstVisible, size)
                }

//            for (i in firstVisible..lastVisible) {
//                val place = cafeAdapter.itemList.getOrNull(i) ?: continue
//                if (!place.isImgRequested) {
//                    place.isImgRequested = true
//                    // 커피 전문점 만 이미지 검색
////                    if(place.category_name.contains(getString(R.string.coffee_shop))){
////                        // query = 주소(서울, 경기 등) + " " + 카페 이름
////                        val address = if(place.address_name.isEmpty()) place.address_name else place.road_address_name
////                        val city = address.split(" ").firstOrNull() ?: ""
////                        val query = city + " " + place.place_name + " " + getString(R.string.cafe)
////                        viewLifecycleOwner.lifecycleScope.launch {
////                            // 1초에 10개 이상 요청 -> HTTP 429 = Too Many Requests
////                            val imageUrl = requestCafeImage(query)
////                            cafeAdapter.updateImage(i, imageUrl)
////                        }
////                    }
//                    // 모두 이미지 검색
//                    val address =
//                        if (place.address_name.isEmpty()) place.address_name else place.road_address_name
//                    val city = address.split(" ").firstOrNull() ?: ""
//                    val query = city + " " + place.place_name + " " + getString(R.string.cafe)
//                    viewLifecycleOwner.lifecycleScope.launch {
//                        // 1초에 10개 이상 요청 -> HTTP 429 = Too Many Requests
//                        val imageUrl = requestCafeImage(query)
//                        cafeAdapter.updateImage(i, imageUrl)
//                    }
//                }
            }

            if (!binding.recyclerviewHome.canScrollVertically(1)) {

                if (cafeAdapter.itemCount < totalCnt) {
                    // 다음 페이지 요청
                    if (binding.progressbar.isGone) { // 중복 요청 방지
                        page++
                        viewLifecycleOwnerLiveData.value?.let{
                            it.lifecycleScope.launch {
                                requestSearchCafesFromKakao()
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
    }

    fun setCategoryList() {
        categoryItems = arrayOf(
            getString(R.string.coffee_shop),
            getString(R.string.dessert_cafe),
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
                        when(it.status){
                            200 -> {
                                it.data?.let{
                                    hashtagAdapter = HomeHashtagAdapter(requireContext(), it, this@HomeFragment)
                                    binding.recyclerviewHomeHashtag.adapter = hashtagAdapter
                                }
                            }
                            400 -> {
                                Log.d(TAG, "400 -> ${it.data}")
                            }
                            else -> {}
                        }
                    }
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
                        }
                        cafeAdapter.addItems(it.documents)

                        isLoadingGetHomeList = true

                        // 서버에 카페 정보 검색
                        val userId = Utils.userId
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
                binding.progressbar.visibility = View.GONE
            }

            override fun onFailure(call: Call<KakaoSearchPlaceResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "${t.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "${t.message}")
                binding.progressbar.visibility = View.GONE

            }
        })

        binding.edtSearchHome.clearFocus()
    }

    suspend fun requestHomeCafeList(userId: Int, placeIds: List<String>){
        try{
            val request = HomeCafeRequest(userId, placeIds)
            val response = RetrofitHelper.getMyService().getHomeCafeList(request)
            val status = response.status
            when(status) {
                200 -> {
                    response.data?.let { data ->
                        cafeAdapter.updateDBdata(data) // DB data로 카페 리스트 업데이트

                        // 방문 인증 사진 없는 것들은 이미지 검색
//                        val nullIndexes = data.withIndex()
//                            .filter { it.value.visit_datas == null }
//                            .map { it.index }
//                        if(nullIndexes.isNotEmpty()){
//                            // 네이버 이미지 검색
//                            viewLifecycleOwnerLiveData.value?.let{
//                                lifecycleScope.launch {
//                                    requestSearchNaverImage(nullIndexes[0], data.size-nullIdx)
//                                }
//                            }
//                        }

                        val nullIdx = data.indexOfFirst { it.visit_datas == null }
                        if(nullIdx < 5){ // 처음에는 화면에 보이는 리스트만 확인
                            viewLifecycleOwnerLiveData.value?.let{
                                lifecycleScope.launch {
                                    requestSearchNaverImage(0, 5)
                                }
                            }
                        }

                    }

                }
                400 -> {
                    Log.d(TAG, "400 -> ${response.data}")
                }
            }
        }catch (e:Exception){
            Log.e(TAG, "홈 데이터 요청 실패: ${e.message}")
        }finally {
            isLoadingGetHomeList = false
        }
    }

    override fun onItemSelected(type: Int) {

        hideKeyboard()
        page = 1
        totalCnt = 0

        when(type){
            Constants.CATEGORY_TYPE -> { // 카테고리 클릭 시 카카오 카페 리스트 요청
                viewLifecycleOwnerLiveData.value?.let{
                    lifecycleScope.launch {  requestSearchCafesFromKakao() }
                }
            }
            Constants.HASHTAG_TYPE -> { // 해시태그 클릭 시 서버에 해당하는 카페 id 리스트 요청
                if(hashtagAdapter.getHashtagList().isNotEmpty()){
                    viewLifecycleOwnerLiveData.value?.let {
                        lifecycleScope.launch { requestSearchCafeIdsFromDB() }
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
    fun requestSearchCafeIdsFromDB() {
        binding.progressbar.visibility = View.VISIBLE

        var query = (searchQuery + " " + categoryAdapter.getSelectedCategory()).trim()

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
                        }
                        cafeAdapter.addItems(it.documents)

                        isLoadingGetHomeList = true

                        // 서버에 카페 정보 검색
                        val userId = Utils.userId
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
                binding.progressbar.visibility = View.GONE
            }

            override fun onFailure(call: Call<KakaoSearchPlaceResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "${t.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "${t.message}")
                binding.progressbar.visibility = View.GONE

            }
        })

        binding.edtSearchHome.clearFocus()
    }

}