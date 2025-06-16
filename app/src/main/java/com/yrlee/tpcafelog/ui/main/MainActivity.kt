package com.yrlee.tpcafelog.ui.main

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityMainBinding
import com.yrlee.tpcafelog.model.KakaoSearchPlaceResponse
import com.yrlee.tpcafelog.ui.favorite.FavoriteFragment
import com.yrlee.tpcafelog.ui.home.HomeFragment
import com.yrlee.tpcafelog.ui.mypage.MypageFragment
import com.yrlee.tpcafelog.ui.review.ReviewAddActivity
import com.yrlee.tpcafelog.ui.review.ReviewFragment
import com.yrlee.tpcafelog.ui.visit.VisitCertifyActivity
import com.yrlee.tpcafelog.util.LocationUtils
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var isFabMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.bab.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }
        binding.bnv.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }

        supportFragmentManager.beginTransaction().add(R.id.fragment_container, HomeFragment()).commit()
        binding.bnv.setOnItemSelectedListener {
            when(it.itemId){
                R.id.bnv_home -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                R.id.bnv_review -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ReviewFragment()).commit()
                R.id.bnv_favorite -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FavoriteFragment()).commit()
                R.id.bnv_mypage -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MypageFragment()).commit()
            }
            true
        }


        // FloatingButton 리스너
        binding.fabMain.setOnClickListener {
            if(!isFabMenuOpen){
                binding.fabMenu.visibility = View.VISIBLE
                binding.fabMain.setImageResource(R.drawable.ic_close)
                isFabMenuOpen = true
                ObjectAnimator.ofFloat(it, "rotationX", 360f).start()
            }else {
                binding.fabMenu.visibility = View.GONE
                binding.fabMain.setImageResource(R.drawable.ic_add)
                isFabMenuOpen = false
                ObjectAnimator.ofFloat(it, "rotationX", -360f).start()
            }

        }

        binding.fabVisit.setOnClickListener {
            startActivity(Intent(this, VisitCertifyActivity::class.java))
        }
        binding.fabReview.setOnClickListener {
            startActivity(Intent(this, ReviewAddActivity::class.java))
        }

    }

    override fun onRestart() {
        super.onRestart()

        if(isFabMenuOpen){
            binding.fabMenu.visibility = View.GONE
            binding.fabMain.setImageResource(R.drawable.ic_add)
            isFabMenuOpen = false
        }
    }

    // 내 위치 검색 작업
//    fun requestMyLocation() {
//        // 요청 객체 생성 [정확도 우선, 5초마다 갱신]
//        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
//
//        // 명시적으로 퍼미션 체크 코드가 이 메소드(requestLocationUpdate())와 같은 영역에 있어야 함.
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            return
//
//        lifecycleScope.launch {
//            locationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
//        }
//    }
//
//    // 내 위치가 갱신될 때 마다 반응하는 콜백 객체
//    val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(p0: LocationResult) {
//            super.onLocationResult(p0)
//            myLocation = p0.lastLocation // 마지막 검색된 위치
//
//            // 위치 탐색이 종료되었으니, 위치 업데이트 멈추기
//            locationProviderClient.removeLocationUpdates(this) // this : LocationCallback 객체
//
//            // 내 위치를 찾았으니 카카오 로컬(장소) 검색 시작
//            lifecycleScope.launch {
//                requestSearchCafes()
//            }
//
//        }
//    }


}