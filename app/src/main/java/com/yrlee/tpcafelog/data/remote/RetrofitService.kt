package com.yrlee.tpcafelog.data.remote

import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitService {

    // DB에 User 추가
    @POST("insertUser.php")
    fun insertUser(
        @Body data: User
    ): Call<MyResponse<String>>

    @GET("loadUser.php")
    fun loadUser(): Call<MyResponse<User>>
}