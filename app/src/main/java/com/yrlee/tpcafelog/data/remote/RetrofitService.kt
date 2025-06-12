package com.yrlee.tpcafelog.data.remote

import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.User
import com.yrlee.tpcafelog.model.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitService {

    // DB에 User 추가
    @Multipart
    @POST("insertUserInfo.php")
    fun postUserInfo(
        @Part("data") data: RequestBody,
        @Part img: MultipartBody.Part?
    ): Call<MyResponse<String>>

    @GET("loadUserInfo.php")
    fun getUserInfo(): Call<MyResponse<UserResponse>>
}