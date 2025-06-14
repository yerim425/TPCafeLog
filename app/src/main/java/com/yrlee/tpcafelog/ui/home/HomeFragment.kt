package com.yrlee.tpcafelog.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.data.remote.RetrofitService
import com.yrlee.tpcafelog.databinding.FragmentHomeBinding
import com.yrlee.tpcafelog.model.KakaoSearchPlaceResponse
import com.yrlee.tpcafelog.model.NaverSearchImageResponse
import com.yrlee.tpcafelog.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.yrlee.tpcafelog.util.LocationUtils

class HomeFragment : Fragment(), OnCategoryItemClickListener {
    val TAG = "home fragment"

    lateinit var binding: FragmentHomeBinding
    private lateinit var categoryItems: Array<String>
    private lateinit var categoryQuery: String
    private var searchQuery: String = ""
    private var page = 1
    private var totalCnt = 0
    var myLocation: Location? = null // 내 위치


    // adapter
    lateinit var categoryAdapter: HomeCategoryAdapter
    lateinit var cafeAdapter: HomeCafeAdapter

    val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it){
            viewLifecycleOwner.lifecycleScope.launch {
                requestSearchCafes()
            }
            Toast.makeText(requireContext(), getString(R.string.message_restricted_search_func), Toast.LENGTH_SHORT).show()
        }else{
            viewLifecycleOwner.lifecycleScope.launch {
                requestSearchCafes()
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
                viewLifecycleOwner.lifecycleScope.launch {
                    requestSearchCafes()
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
                viewLifecycleOwner.lifecycleScope.launch {
                    requestSearchCafes()
                }
                binding.edtSearchHome.setText(searchQuery)
                binding.edtSearchHome.clearFocus()

                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtSearchHome.windowToken, 0)
            }
            false
        }

        // 리사이클러뷰 스크롤 리스너
        binding.recyclerviewHome.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            // 스크롤 시 화면에 보이는 카페들의 이미지 요청
            val layoutManager = binding.recyclerviewHome.layoutManager as LinearLayoutManager
            val firstVisible = layoutManager.findFirstVisibleItemPosition()
            val lastVisible = layoutManager.findLastVisibleItemPosition()
            for (i in firstVisible..lastVisible) {
                val place = cafeAdapter.itemList.getOrNull(i) ?: continue
                if (!place.isImgRequested) {
                    place.isImgRequested = true
//                    if(place.category_name.contains(getString(R.string.coffee_shop))){
//                        // query = 주소(서울, 경기 등) + " " + 카페 이름
//                        val address = if(place.address_name.isEmpty()) place.address_name else place.road_address_name
//                        val city = address.split(" ").firstOrNull() ?: ""
//                        val query = city + " " + place.place_name + " " + getString(R.string.cafe)
//                        viewLifecycleOwner.lifecycleScope.launch {
//                            // 1초에 10개 이상 요청 -> HTTP 429 = Too Many Requests
//                            val imageUrl = requestCafeImage(query)
//                            cafeAdapter.updateImage(i, imageUrl)
//                        }
//                    }
                    val address =
                        if (place.address_name.isEmpty()) place.address_name else place.road_address_name
                    val city = address.split(" ").firstOrNull() ?: ""
                    val query = city + " " + place.place_name + " " + getString(R.string.cafe)
                    viewLifecycleOwner.lifecycleScope.launch {
                        // 1초에 10개 이상 요청 -> HTTP 429 = Too Many Requests
                        val imageUrl = requestCafeImage(query)
                        cafeAdapter.updateImage(i, imageUrl)
                    }
                }
            }

            if (!binding.recyclerviewHome.canScrollVertically(1)) {

                if (cafeAdapter.itemCount < totalCnt) {
                    // 다음 페이지 요청
                    if (binding.progressbar.isGone) { // 중복 요청 방지
                        page++
                        viewLifecycleOwner.lifecycleScope.launch {
                            requestSearchCafes()
                        }
                    }
                }

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

//    fun clickCategory(v: View){
//
//        view?.findViewById<CheckBox>(choiceCategoryId)?.isChecked = false
//        choiceCategoryId = v.id
//        when(v.id){
//            R.id.tv_coffee_shop -> categoryQuery = getString(R.string.coffee_shop)
//            R.id.tv_dessert -> categoryQuery = getString(R.string.dessert)
//            R.id.tv_cartoon -> categoryQuery = getString(R.string.cartoon_cafe)
//            R.id.tv_board -> categoryQuery = getString(R.string.board_cafe)
//            R.id.tv_study -> categoryQuery = getString(R.string.study_cafe)
//            R.id.tv_kids -> categoryQuery = getString(R.string.kids_cafe)
//            R.id.tv_book -> categoryQuery = getString(R.string.book_cafe)
//            R.id.tv_live -> categoryQuery = getString(R.string.live_cafe)
//            R.id.tv_gallery -> categoryQuery = getString(R.string.galley_cafe)
//            R.id.tv_fruits -> categoryQuery = getString(R.string.fruits_shop)
//            R.id.tv_meeting -> categoryQuery = getString(R.string.meeting_space)
//            R.id.tv_dog -> categoryQuery = getString(R.string.dogs_cafe)
//            R.id.tv_cat -> categoryQuery = getString(R.string.cats_cafe)
//        }
//
//        // "검색 단어 + categoryQuery"로 키워드 검색 요청
//
//        binding.edtSearchHome.clearFocus()
//        choiceCategoryId = v.id
//    }

    fun setHashtagList() {
        // DB에서 가져와서 RecyclerView 설정 // 지금은 더미데이터로...
        val items = resources.getStringArray(R.array.cafe_tags)
        binding.recyclerviewHomeHashtag.adapter =
            HomeHashtagAdapter(requireContext(), items.toList())
    }

    // 카페 리스트 요청
    fun requestSearchCafes() {
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
                        }
                        cafeAdapter.addItems(it.documents)

                        // 일단 앞에 5개만 이미지 요청
                        for (i in 0 until 5) {
                            val place = cafeAdapter.itemList.getOrNull(i) ?: continue
                            if (!place.isImgRequested) {
                                place.isImgRequested = true
                                val address =
                                    if (place.address_name.isEmpty()) place.address_name else place.road_address_name
                                val city = address.split(" ").firstOrNull() ?: ""
                                val query =
                                    city + " " + place.place_name + " " + getString(R.string.cafe)
                                viewLifecycleOwner.lifecycleScope.launch {
                                    // 1초에 10개 이상 요청 -> HTTP 429 = Too Many Requests
                                    val imageUrl = requestCafeImage(query)
                                    cafeAdapter.updateImage(i, imageUrl)
                                }
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

    suspend fun requestCafeImage(query: String): String? {
        return try {
            val response = RetrofitHelper.getNaverService().getSearchImage(query)
            response.items.firstOrNull()?.link
        } catch (e: Exception) {
            Log.e(TAG, "이미지 요청 실패 : ${e.message}")
            null
        }
    }

    override fun onCategoryItemSelected() {
        // 카테고리 클릭 시 카페 리스트 요청
        viewLifecycleOwner.lifecycleScope.launch {
            requestSearchCafes()
        }
    }
}