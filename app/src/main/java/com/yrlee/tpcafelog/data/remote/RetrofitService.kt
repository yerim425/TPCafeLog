package com.yrlee.tpcafelog.data.remote

import com.yrlee.tpcafelog.MyApplication
import com.yrlee.tpcafelog.model.CafeName
import com.yrlee.tpcafelog.model.KakaoSearchPlaceResponse
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.NaverSearchImageResponse
import com.yrlee.tpcafelog.model.User
import com.yrlee.tpcafelog.model.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface RetrofitService {
    val kakaoRestApiKey: String
        get() = MyApplication.KAKAO_REST_API_KEY

    // DB에 User 추가
    @Multipart
    @POST("insertUserInfo.php")
    fun postUserInfo(
        @Part("data") data: RequestBody,
        @Part img: MultipartBody.Part?
    ): Call<MyResponse<String>>

    // DB에서 User 데이터 가져오기
    @GET("loadUserInfo.php")
    fun getUserInfo(): Call<MyResponse<UserResponse>>

    // 카카오 키워드로 장소 검색하기
    @GET("v2/local/search/keyword.json?category_group_code=CE7")
    fun getSearchCafes(
        @Query("query") query: String,
        @Query("x") longitude: String?= null,
        @Query("y") latitude: String?= null,
        @Query("page") page: Int,
    ): Call<KakaoSearchPlaceResponse>

    // 100m 근방의 카페 검색하기
    @GET("v2/local/search/keyword.json?category_group_code=CE7&radius=1000")
    suspend fun getSearchCafeNames(
        @Query("query") query: String,
        @Query("x") longitude: String,
        @Query("y") latitude: String,
        @Query("page") page: Int,
    ): KakaoSearchPlaceResponse

    // 네이버 이미지 검색하기
    @GET("v1/search/image?display=1")
    suspend fun getSearchImage(
        @Query("query") query: String,
    ): NaverSearchImageResponse
}