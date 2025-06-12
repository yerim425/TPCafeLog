package com.yrlee.tpcafelog.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitHelper {
    private val kakaoRetrofit by lazy {
        Retrofit.Builder().run {
            baseUrl("https://dapi.kakao.com")
            addConverterFactory(ScalarsConverterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
            build()
        }
    }
    fun getKakaoService(): RetrofitService{
        return kakaoRetrofit.create(RetrofitService::class.java)
    }

    private val myRetrofit by lazy {
        Retrofit.Builder().run {
            baseUrl("http://yrlee2025.dothome.co.kr/cafelog/")
            addConverterFactory(ScalarsConverterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
            build()
        }
    }
    fun getMyService(): RetrofitService{
        return myRetrofit.create(RetrofitService::class.java)
    }
}