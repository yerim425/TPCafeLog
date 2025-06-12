package com.yrlee.tpcafelog

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.kakao.vectormap.KakaoMapSdk
import androidx.core.content.edit
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.yrlee.tpcafelog.util.PrefUtils
import com.yrlee.tpcafelog.util.Utils

class MyApplication : Application(){

    companion object{
        val KAKAO_NATIVE_API_KEY = BuildConfig.KAKAO_NATIVE_API_KEY
        val KAKAO_REST_API_KEY = BuildConfig.KAKAO_REST_API_KEY
        val NAVER_CLIENT_ID = BuildConfig.NAVER_CLIENT_ID
        val NAVER_CLIENT_SECRET = BuildConfig.NAVER_CLIENT_SECRET
    }

    override fun onCreate() {
        super.onCreate()
        PrefUtils.init(applicationContext)
        Utils.init(applicationContext)

        // 카카오 로그인 SDK 초기화
        KakaoSdk.init(this, KAKAO_NATIVE_API_KEY)

        // 카카오 지도 SDK 초기화
        KakaoMapSdk.init(this, KAKAO_NATIVE_API_KEY)
    }
}