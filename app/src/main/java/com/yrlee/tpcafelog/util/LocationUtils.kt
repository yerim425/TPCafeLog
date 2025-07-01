package com.yrlee.tpcafelog.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yrlee.tpcafelog.R
import kotlinx.coroutines.launch

object LocationUtils {

    private lateinit var appContext: Context
    val locationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(appContext)
    }


    fun init(context: Context) {
        appContext = context.applicationContext
    }


    fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
    }

    // 내 위치 얻어오기 [개인정보이기에 동적퍼미션 필요]
    // 내 위치 검색은 Google Fused Location API 사용 [라이브러리 추가 필요 : play-services-location]
    // 내 위치 정보를 얻어오기 위한 클래스의 참조변수 [위치정보제공자(gps, network, passive)를 사용하는 객체]
    // 내위치 받아오기
    fun requestMyLocation(
        context: Context,
        onResult: (Location?) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            onResult(null)
            return
        }
        val locationRequest = createLocationRequest()

        val callback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                locationProviderClient.removeLocationUpdates(this)
                onResult(p0.lastLocation)
                Log.d("my location", "${p0.lastLocation?.latitude}, ${p0.lastLocation?.longitude}")
            }
        }
        locationProviderClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
    }
}