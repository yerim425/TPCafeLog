package com.yrlee.tpcafelog.data.remote

import com.yrlee.tpcafelog.MyApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitHelper {
    // 카카오 API 연결
    private val kakaoInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "KakaoAK ${MyApplication.KAKAO_REST_API_KEY}")
            .build()
        chain.proceed(request)
    }
    private val kakaoOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(kakaoInterceptor)
        .build()

    private val kakaoRetrofit by lazy {
        Retrofit.Builder().run {
            baseUrl("https://dapi.kakao.com")
            client(kakaoOkHttpClient)
            addConverterFactory(ScalarsConverterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
            build()
        }
    }
    fun getKakaoService(): RetrofitService{
        return kakaoRetrofit.create(RetrofitService::class.java)
    }

    // 네이버 API
    val naverClientId = MyApplication.NAVER_CLIENT_ID
    val naverClientSecret = MyApplication.NAVER_CLIENT_SECRET
    private val naverInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-Naver-Client-Id", naverClientId)
            .addHeader("X-Naver-Client-Secret", naverClientSecret)
            .build()
        chain.proceed(request)
    }
    private val naverOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(naverInterceptor)
        .build()

    private val naverRetrofit by lazy {
        Retrofit.Builder().run{
            baseUrl("https://openapi.naver.com")
            client(naverOkHttpClient)
            addConverterFactory(ScalarsConverterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
            build()
        }
    }
    fun getNaverService(): RetrofitService{
        return naverRetrofit.create(RetrofitService::class.java)
    }

    // 닷홈 서버 연결
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