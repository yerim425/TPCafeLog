package com.yrlee.tpcafelog.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitHelper {

    companion object{
        fun getKakaoRetrofitInstance(): Retrofit {
            val retrofit = Retrofit.Builder().run{
                baseUrl("https://dapi.kakao.com")
                addConverterFactory(ScalarsConverterFactory.create())
                addConverterFactory(GsonConverterFactory.create())
                build()
            }
            return retrofit
        }

        fun getMyRetrofitInstance(): Retrofit {
            val retrofit = Retrofit.Builder().run{
                baseUrl("http://yrlee2025.dothome.co.kr/cafelog/")
                addConverterFactory(ScalarsConverterFactory.create())
                addConverterFactory(GsonConverterFactory.create())
                build()
            }
            return retrofit
        }
    }
}