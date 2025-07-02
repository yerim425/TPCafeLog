package com.yrlee.tpcafelog.ui.map

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.data.remote.RetrofitService
import com.yrlee.tpcafelog.databinding.ActivityMapCafeBinding
import com.yrlee.tpcafelog.model.KakaoSearchPlaceResponse
import com.yrlee.tpcafelog.model.Place
import com.yrlee.tpcafelog.util.LocationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.Exception

class MapCafeActivity : AppCompatActivity() {
    val binding by lazy {ActivityMapCafeBinding.inflate(layoutInflater)}
    lateinit var kakaoMap: KakaoMap
    var myLocation: Location? = null
    var page: Int = 1
    lateinit var labelLayer: LabelLayer

    var cafeList = mutableListOf<Place>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        LocationUtils.requestMyLocation(this){
            myLocation = it
            if(myLocation == null) {
                Toast.makeText(this, "위치를 가져오지 못했어요.", Toast.LENGTH_SHORT).show()
                binding.tvCafeAddress.text = getString(R.string.seoul)
            }else{
                lifecycleScope.launch {
                    requestMyAddress()
                }
            }
            binding.mapview.start(mapLifecycleCallback, mapReadyCallback)
        }

    }

    // 지도 준비가 완료되면 반응하는 콜백 객체
    val mapReadyCallback = object : KakaoMapReadyCallback(){
        override fun onMapReady(p0: KakaoMap) {
            kakaoMap = p0
//            val center: LatLng = kakaoMap.cameraPosition!!.position
//            val lat = center.latitude
//            val lng = center.longitude
            // 현재 내 위치로 지도 카메라 이동 (위치가 null이면 서울 좌표 등록)
            val latitude : Double = myLocation?.latitude ?: 37.550263
            val longitude : Double = myLocation?.longitude ?: 126.997083
            val myPos: LatLng = LatLng.from(latitude, longitude)

            val cameraUpdate = CameraUpdateFactory.newCenterPosition(myPos, 17)
            kakaoMap.moveCamera(cameraUpdate)

            // 내 위치에 마커 추가하기
            labelLayer = kakaoMap.labelManager!!.layer!!
            val labelOptions = LabelOptions.from(myPos).setStyles(R.drawable.ic_location).setTag("mine")
            labelLayer.addLabel(labelOptions)

            // 주변 카페 검색해서 지도에 표시
            lifecycleScope.launch {
                requestSearchCafes()
            }

        }

    }

    // 지도가 종료되거나 에러가 발생하는 상황에 반응하는 콜백 객체
    val mapLifecycleCallback = object : MapLifeCycleCallback(){
        override fun onMapDestroy() {
        }

        override fun onMapError(p0: Exception?) {
            Toast.makeText(this@MapCafeActivity, "${p0?.message}", Toast.LENGTH_SHORT).show()
        }

    }

    // 좌표를 주소로 변환
    suspend fun requestMyAddress(){
        withContext(Dispatchers.IO){
            try {

                val response = RetrofitHelper.getKakaoService().getMyAddress(myLocation!!.longitude.toString(), myLocation!!.latitude.toString())
                val address = response.documents.firstOrNull()?.address?.region_3depth_name ?: "주소 없음"
                withContext(Dispatchers.Main){
                    binding.tvCafeAddress.text = "$address 카페"
                }
            } catch (e: Exception) {
                Log.e("map cafe activity", "error: ${e.message}")
            }
        }
    }

    // 주변 카페 정보 요청하기
    // 카페 리스트 요청
    suspend fun requestSearchCafes() {
        binding.progressbar.visibility = View.VISIBLE

        withContext(Dispatchers.IO){
            try{

                val response = RetrofitHelper.getKakaoService().getSearchMapCafes(
                    longitude = myLocation!!.longitude.toString(),
                    latitude = myLocation!!.latitude.toString(),
                    page = page
                )
                withContext(Dispatchers.Main){
                    response.documents.forEach {
                        if(!cafeList.contains(it)) {
                            val pos = LatLng.from(it.latitude.toDouble(), it.longitude.toDouble())
                            val options = LabelOptions.from(pos).setStyles(R.drawable.ic_location).setTag(it)
                            labelLayer.addLabel(options)
                            cafeList.add(it)
                        }

                    }
                }

            }catch (e: Exception){
                Log.e("map cafe activity", "search cafes error: ${e.message}")
            }
        }
        binding.progressbar.visibility = View.GONE
    }

    fun getCurrentMapBounds(){
//        val mapView: MapView = binding.mapview
//
//        // 1. 현재 중심 좌표
//        val center = mapView.mapCenterPoint
//        val centerGeo = center.mapPointGeoCoord
//        val centerLat = centerGeo.latitude
//        val centerLng = centerGeo.longitude
//
//        // 2. 줌 레벨
//        val zoomLevel = mapView.zoomLevel
//
//        // 3. 계산식으로 bounds 근사 추정
//        val latDelta = 0.02 * (20 - zoomLevel)  // 대략적인 범위 (줌 레벨 작을수록 넓어짐)
//        val lngDelta = 0.025 * (20 - zoomLevel)
//
//        // 4. 남서, 북동 좌표 계산
//        val southWestLat = centerLat - latDelta
//        val southWestLng = centerLng - lngDelta
//        val northEastLat = centerLat + latDelta
//        val northEastLng = centerLng + lngDelta
//
//        val rect = "$southWestLng,$southWestLat,$northEastLng,$northEastLat"
//        Log.d("MapBounds", "rect=$rect") // 카카오 Local API 요청용 rect



        val center: LatLng = kakaoMap.cameraPosition!!.position
        val lat = center.latitude
        val lng = center.longitude

        val zoomLevel = kakaoMap.zoomLevel

    }
}