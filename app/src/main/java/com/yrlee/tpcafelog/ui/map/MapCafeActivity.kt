package com.yrlee.tpcafelog.ui.map

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelTextBuilder
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityMapCafeBinding
import com.yrlee.tpcafelog.model.Place
import com.yrlee.tpcafelog.util.LocationUtils
import com.yrlee.tpcafelog.util.PrefUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapCafeActivity : AppCompatActivity() {
    val binding by lazy { ActivityMapCafeBinding.inflate(layoutInflater) }
    lateinit var kakaoMap: KakaoMap
    var myLocation: Location? = null
    var page: Int = 1
    lateinit var labelLayer: LabelLayer
    lateinit var lastRequestedRect: String
    var isLoading = false
    var debounceJob: Job? = null // 지도 이동 시 카페 요청 job

    val cafeList = mutableListOf<Place>()
    val cafeIdSet = mutableSetOf<String>()
    val cafeLabelMap = mutableMapOf<String, Label>()

    var selectedCafe: Place? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        LocationUtils.requestMyLocation(this) {
            myLocation = it
            if (myLocation == null) {
                Toast.makeText(this, "위치를 가져오지 못했어요.", Toast.LENGTH_SHORT).show()
                binding.tvCafeAddress.text = getString(R.string.seoul)
            } else {
                lifecycleScope.launch {
                    requestMyAddress(myLocation?.latitude.toString(), myLocation?.longitude.toString())
                }
            }

            binding.mapview.start(mapLifecycleCallback, mapReadyCallback)
        }

        binding.btnClose.setOnClickListener { finish() }

    }

    // 지도 준비가 완료되면 반응하는 콜백 객체
    val mapReadyCallback = object : KakaoMapReadyCallback() {
        override fun onMapReady(p0: KakaoMap) {
            kakaoMap = p0
            // 현재 내 위치로 지도 카메라 이동 (위치가 null이면 서울 좌표 등록)
            val latitude: Double = myLocation?.latitude ?: 37.550263
            val longitude: Double = myLocation?.longitude ?: 126.997083
            val myPos: LatLng = LatLng.from(latitude, longitude)
            Log.d("rect", "$myPos")

            kakaoMap.cameraMinLevel = 16
            val cameraUpdate = CameraUpdateFactory.newCenterPosition(myPos, 18)
            kakaoMap.moveCamera(cameraUpdate)
            lastRequestedRect = getCurrentMapRect()

            // 내 위치에 마커 추가하기
            labelLayer = kakaoMap.labelManager!!.layer!!
            val labelTextBuilder = LabelTextBuilder().setTexts("내위치")
            val labelOptions = LabelOptions.from(myPos).setTag("mine").setTexts(labelTextBuilder)
            val img_url = PrefUtils.getString("img_url")
            Glide.with(this@MapCafeActivity)
                .asBitmap()
                .load(img_url)
                .circleCrop()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        labelOptions.setStyles(resource)
                        labelLayer.addLabel(labelOptions)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        TODO("Not yet implemented")
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        labelOptions.setStyles(R.drawable.ic_my_location_pin)
                        labelLayer.addLabel(labelOptions)
                    }
                })

            // 주변 카페 검색해서 지도에 표시
            lifecycleScope.launch {
                requestSearchCafes()
            }

            // 지도 이동시 현재 카메라 위치에서의 새로운 카페 요청
            kakaoMap.setOnCameraMoveEndListener { kakaoMap, cameraPosition, gestureType ->
                debounceJob?.cancel()
                debounceJob = lifecycleScope.launch {
                    delay(500) // 0.5초 동안 추가 이동 없을 때만 요청
                    requestSearchCafes()
                }
            }

        }

    }

    // 지도가 종료되거나 에러가 발생하는 상황에 반응하는 콜백 객체
    val mapLifecycleCallback = object : MapLifeCycleCallback() {
        override fun onMapDestroy() {
        }

        override fun onMapError(p0: Exception?) {
            Toast.makeText(this@MapCafeActivity, "${p0?.message}", Toast.LENGTH_SHORT).show()
        }

    }

    // 좌표를 주소로 변환
    suspend fun requestMyAddress(lat: String?, lng: String?) {
        if (lat == null || lng == null) {
            binding.tvCafeAddress.text = "카페"
            return
        }
        try {
            val response = RetrofitHelper.getKakaoService().getMyAddress(lng, lat)
            val address = response.documents.firstOrNull()?.address?.region_3depth_name ?: ""
            withContext(Dispatchers.Main) {
                binding.tvCafeAddress.text = "$address 카페".trim()
            }
        } catch (e: Exception) {
            Log.e("map cafe activity", "error: ${e.message}")
        }
    }

    // 주변 카페 정보 요청하기
    // 카페 리스트 요청
    suspend fun requestSearchCafes() {
        try {
            val currentRect = getCurrentMapRect() // "swLng,swLat,neLng,neLat"
            if (currentRect == lastRequestedRect) return
            lastRequestedRect = currentRect

            binding.progressbar.visibility = View.VISIBLE

            // 화면 밖으로 나간 카페들은 마커 제거
            val iterator = cafeList.iterator()
            while (iterator.hasNext()) {
                val cafe = iterator.next()
                if (!isCafeInsideRect(cafe)) {
                    iterator.remove()
                    cafeIdSet.remove(cafe.id)
                    val removeLabel = cafeLabelMap.remove(cafe.id)
                    removeLabel?.let {
                        labelLayer.remove(it)
                    }
                }
            }

            // 현재 카메라 위치를 center로 하여 지도 화면 내의 모든 카페 정보 요청
            val centerPos = kakaoMap.cameraPosition?.position
            if (centerPos == null) {
                Log.e("map cafe activity", "kakao pos is null")
                return
            }
            val lat = centerPos.latitude.toString()
            val lng = centerPos.longitude.toString()

            requestMyAddress(lat, lng)

            page = 1
            while (true) {
                // 카페 정보 요청(1번에 최대 15개)
                val response = withContext(Dispatchers.IO) {
                    RetrofitHelper.getKakaoService().getSearchMapCafes(
                        longitude = lng,
                        latitude = lat,
                        page = page,
                        rect = currentRect
                    )
                }
                val cafes = response.documents
                if (cafes.isEmpty()) break

                // 카페들 마커 ui 표시
                withContext(Dispatchers.Main) {

                    for (cafe in cafes) {
                        if (cafeIdSet.contains(cafe.id)) continue
                        val pos =
                            LatLng.from(cafe.latitude.toDouble(), cafe.longitude.toDouble())
                        val options = LabelOptions.from(pos)
                            .setStyles(R.drawable.ic_location)
                            .setTag(cafe.id)

                        val label = labelLayer.addLabel(options)
                        cafeLabelMap[cafe.id] = label
                        cafeList.add(cafe)
                        cafeIdSet.add(cafe.id)
                    }
                }
                if (cafes.size < 15 || page == 3) break // 마지막 페이지
                page++
            }
        } catch (e: Exception) {
            Log.e("map cafe activity", "search cafes error: ${e.message}")
        } finally {
            binding.progressbar.visibility = View.GONE
        }
    }

    fun getCurrentMapRect(): String {

        val center: LatLng = kakaoMap.cameraPosition!!.position
        val centerLat = center.latitude
        val centerLng = center.longitude

        val zoomLevel = kakaoMap.zoomLevel

        val latDelta = 0.02 * (20 - zoomLevel)
        val lngDelta = 0.025 * (20 - zoomLevel)

        val swLat = centerLat - latDelta
        val swLng = centerLng - lngDelta
        val neLat = centerLat + latDelta
        val neLng = centerLng + lngDelta

        val rect = "$swLng,$swLat,$neLng,$neLat"
        Log.d("rect", "${rect}")
        return rect
    }

    fun isCafeInsideRect(cafe: Place): Boolean {
        val (swLng, swLat, neLng, neLat) = lastRequestedRect.split(",").map { it.toDouble() }

        val lat = cafe.latitude.toDouble()
        val lng = cafe.longitude.toDouble()

        return lat in swLat..neLat && lng in swLng..neLng
    }

}