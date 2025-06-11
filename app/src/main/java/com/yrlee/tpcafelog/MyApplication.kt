package com.yrlee.tpcafelog

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.kakao.vectormap.KakaoMapSdk
import androidx.core.content.edit
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient

class MyApplication : Application(){

    companion object{
        val KAKAO_NATIVE_API_KEY = BuildConfig.KAKAO_NATIVE_API_KEY
        val KAKAO_REST_API_KEY = BuildConfig.KAKAO_REST_API_KEY
        val NAVER_CLIENT_ID = BuildConfig.NAVER_CLIENT_ID
        val NAVER_CLIENT_SECRET = BuildConfig.NAVER_CLIENT_SECRET

        private lateinit var sharedPreferences: SharedPreferences

        fun putString(key: String, value: String){
            sharedPreferences.edit(commit = true) { putString(key, value) }
        }

        fun getString(key: String): String? {
            return sharedPreferences.getString(key, null)
        }

        fun putInt(key: String, value: Int){
            sharedPreferences.edit(commit = true) { putInt(key, value) }
        }

        fun getInt(key: String): Int {
            return sharedPreferences.getInt(key, -1)
        }

        fun putBoolean(key: String, value: Boolean){
            sharedPreferences.edit(commit = true) { putBoolean(key, value) }
        }

        fun getBoolean(key: String): Boolean{
            return sharedPreferences.getBoolean(key, false)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = applicationContext.getSharedPreferences(getString(R.string.shared_prefs_name), MODE_PRIVATE)

        // 카카오 로그인 SDK 초기화
        KakaoSdk.init(this, KAKAO_NATIVE_API_KEY)

        // 카카오 지도 SDK 초기화
        KakaoMapSdk.init(this, KAKAO_NATIVE_API_KEY)
    }
}