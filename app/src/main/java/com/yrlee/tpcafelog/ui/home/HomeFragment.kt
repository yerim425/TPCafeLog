package com.yrlee.tpcafelog.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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

class HomeFragment : Fragment(), OnCategoryItemClickListener {
    val TAG = "home fragment"

    lateinit var binding: FragmentHomeBinding
    private lateinit var categoryItems: Array<String>
    private lateinit var categoryQuery: String
    private var searchQuery: String = ""
    private var page = 1
    private var totalCnt = 0

    // adapter
    lateinit var categoryAdapter: HomeCategoryAdapter
    lateinit var cafeAdapter: HomeCafeAdapter


    // 내 위치 얻어오기 [개인정보이기에 동적퍼미션 필요]
    // 내 위치 검색은 Google Fused Location API 사용 [라이브러리 추가 필요 : play-services-location]
    // 내 위치 정보를 얻어오기 위한 클래스의 참조변수 [위치정보제공자(gps, network, passive)를 사용하는 객체]
    val locationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            requireContext()
        )
    }
    val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) requestMyLocation()
            else {
                requestSearchCafes()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.message_restricted_search_func),
                    Toast.LENGTH_SHORT
                ).show()
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


        cafeAdapter = HomeCafeAdapter(requireContext())
        binding.recyclerviewHome.adapter = cafeAdapter


        // 내 위치 정보 취득에 대한 동적 퍼미션
        val permissionResult =
            requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionResult == PackageManager.PERMISSION_DENIED) permissionResultLauncher.launch(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) // 퍼미션 요청
        else requestMyLocation()


        // 검색 리스너
        binding.edtSearchHome.setOnEditorActionListener { v, actionId, event ->
            searchQuery = binding.edtSearchHome.text.toString().trim()
            if(searchQuery.isEmpty()){
                Toast.makeText(requireContext(), getString(R.string.message_input_search_query), Toast.LENGTH_SHORT).show()
            }else{
                categoryAdapter.setUnselect()
                requestSearchCafes()
                binding.edtSearchHome.setText(searchQuery)
                binding.edtSearchHome.clearFocus()

                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtSearchHome.windowToken, 0)
            }
            false
        }

        // 리사이클러뷰 스크롤 리스너
        binding.recyclerviewHome.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            val layoutManager = binding.recyclerviewHome.layoutManager as LinearLayoutManager
            val firstVisible = layoutManager.findFirstVisibleItemPosition()
            val lastVisible = layoutManager.findLastVisibleItemPosition()
            for(i in firstVisible..lastVisible){
                val place = cafeAdapter.itemList.getOrNull(i) ?: continue
                if(!place.isImgRequested){
                    place.isImgRequested = true
                    if(place.category_name.contains(getString(R.string.coffee_shop))){
                        // query = 주소(서울, 경기 등) + " " + 카페 이름
                        val address = if(place.address_name.isEmpty()) place.address_name else place.road_address_name
                        val city = address.split(" ").firstOrNull() ?: ""
                        val query = city + " " + place.place_name + " " + getString(R.string.cafe)
                        lifecycleScope.launch {
                            // 1초에 10개 이상 요청 -> HTTP 429 = Too Many Requests
                            //delay(150L)
                            val imageUrl = requestCafeImage(query)
                            //val pos = if(page==1) i else cafeAdapter.itemCount - it.documents.size + i
                            cafeAdapter.updateImage(i, imageUrl)
                        }
                    }
                }
            }

            if(!binding.recyclerviewHome.canScrollVertically(1)){

                // 다음 페이지 요청
                if(binding.progressbar.isGone){ // 중복 요청 방지
                    page++
                    requestSearchCafes()
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

    // 내 위치 검색 작업
    fun requestMyLocation() {
        // 요청 객체 생성 [정확도 우선, 5초마다 갱신]
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        // 명시적으로 퍼미션 체크 코드가 이 메소드(requestLocationUpdate())와 같은 영역에 있어야 함.
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            return

        locationProviderClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // 내 위치가 갱신될 때 마다 반응하는 콜백 객체
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            (requireContext() as MainActivity).myLocation = p0.lastLocation // 마지막 검색된 위치

            // 위치 탐색이 종료되었으니, 위치 업데이트 멈추기
            locationProviderClient.removeLocationUpdates(this) // this : LocationCallback 객체

            // 내 위치를 찾았으니 카카오 로컬(장소) 검색 시작
            requestSearchCafes()
        }
    }

    // 카페 리스트 요청
    fun requestSearchCafes() {
        binding.progressbar.visibility = View.VISIBLE

        var query = (searchQuery + " " + categoryAdapter.getSelectedCategory()).trim()

        val call = RetrofitHelper.getKakaoService().getSearchCafes(
            query = query,
            page = page,
            longitude = (requireContext() as MainActivity).myLocation?.longitude?.toString(),
            latitude = (requireContext() as MainActivity).myLocation?.longitude?.toString()
        )
        call.enqueue(object : Callback<KakaoSearchPlaceResponse> {
            override fun onResponse(
                call: Call<KakaoSearchPlaceResponse>,
                response: Response<KakaoSearchPlaceResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d(TAG, "cafe 리스트 요청 성공 ${response.body()?.meta?.total_count}")
                    val body = response.body()
                    body?.let {
                        if(page==1){
                            cafeAdapter.clearItemList()
                            totalCnt = it.meta.total_count
                        }
                        cafeAdapter.addItems(it.documents)

                        // 카테고리에 커피전문점이 포함되어 있는 카페들만 네이버 이미지 검색
//                        for(i in 0  until it.documents.size){
//                            val place = it.documents[i]
//                            if(place.category_name.contains(getString(R.string.coffee_shop))){
//                                // query = 주소(서울, 경기 등) + " " + 카페 이름
//                                val address = if(place.address_name.isEmpty()) place.address_name else place.road_address_name
//                                val city = address.split(" ").firstOrNull() ?: ""
//                                val query = city + " " + place.place_name
//                                lifecycleScope.launch {
//                                    // 1초에 10개 이상 요청 -> HTTP 429 = Too Many Requests
//                                    delay(150L)
//                                    val imageUrl = requestCafeImage(query)
//                                    val pos = if(page==1) i else cafeAdapter.itemCount - it.documents.size + i
//                                    cafeAdapter.updateImage(pos, imageUrl)
//                                }
//                            }
//                        }
                    }
                } else {
                    Log.d(TAG, response.errorBody()?.string().toString())
                }
                binding.progressbar.visibility = View.GONE
            }

            override fun onFailure(call: Call<KakaoSearchPlaceResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "${t.message}", Toast.LENGTH_SHORT).show()
                binding.progressbar.visibility = View.GONE

            }
        })

        binding.edtSearchHome.clearFocus()
    }

    suspend fun requestCafeImage(query: String): String?{
        return try{
            val response = RetrofitHelper.getNaverService().getSearchImage(query)
            response.items.firstOrNull()?.link
        }catch (e: Exception){
            Log.e(TAG, "이미지 요청 실패 : ${e.message}")
            null
        }
    }

    override fun onCategoryItemSelected() {
        // 카테고리 클릭 시 카페 리스트 요청
        requestSearchCafes()
    }


}