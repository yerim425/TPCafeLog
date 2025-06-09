package com.yrlee.tpcafelog

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk

class MyApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        KakaoMapSdk.init(this, "1913a6e7245d4780369849e0fdfbca26")
    }
}