package com.yrlee.tpcafelog.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.yrlee.tpcafelog.ui.review.ReviewFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var myLocation: Location? = null // 내 위치

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




    }




}