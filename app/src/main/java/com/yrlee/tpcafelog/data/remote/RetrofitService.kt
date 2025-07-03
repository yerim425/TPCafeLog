package com.yrlee.tpcafelog.data.remote

import android.graphics.Rect
import com.yrlee.tpcafelog.MyApplication
import com.yrlee.tpcafelog.model.HashTagItem
import com.yrlee.tpcafelog.model.HomeCafeRequest
import com.yrlee.tpcafelog.model.HomeCafeResponse
import com.yrlee.tpcafelog.model.HomeHashtagFilteringRequest
import com.yrlee.tpcafelog.model.HomeHashtagFilteringResponse
import com.yrlee.tpcafelog.model.KakaoAddressResponse
import com.yrlee.tpcafelog.model.KakaoSearchPlaceResponse
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.NaverSearchImageResponse
import com.yrlee.tpcafelog.model.ReviewListItemResponse
import com.yrlee.tpcafelog.model.UserInfoRequest
import com.yrlee.tpcafelog.model.UserInfoResponse
import com.yrlee.tpcafelog.model.VisitedCafeItem
import com.yrlee.tpcafelog.model.PointUpdateResponse
import com.yrlee.tpcafelog.model.ReviewDetailRequest
import com.yrlee.tpcafelog.model.ReviewDetailResponse
import com.yrlee.tpcafelog.model.ReviewLikeRequest
import com.yrlee.tpcafelog.model.ReviewLikeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
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
    ): Call<MyResponse<UserInfoResponse>>

    // DB에서 User 데이터 가져오기
    @POST("loadUserInfo.php")
    fun getUserInfo(
        @Body data: UserInfoRequest
    ): Call<MyResponse<UserInfoResponse?>>

    // 카카오 키워드로 장소 검색하기
    @GET("v2/local/search/keyword.json?category_group_code=CE7")
    fun getSearchCafes(
        @Query("query") query: String,
        @Query("x") longitude: String?= null,
        @Query("y") latitude: String?= null,
        @Query("page") page: Int,
    ): Call<KakaoSearchPlaceResponse>

    // 100m 근방의 카페 검색하기
    @GET("v2/local/search/keyword.json?category_group_code=CE7&radius=100")
    suspend fun getSearchCafeNames(
        @Query("query") query: String,
        @Query("x") longitude: String,
        @Query("y") latitude: String,
        @Query("page") page: Int,
    ): KakaoSearchPlaceResponse

    // 내 위치 좌표를 주소로 변환하기
    @GET("/v2/local/geo/coord2address.json")
    suspend fun getMyAddress(
        @Query("x") longitude: String?,
        @Query("y") latitude: String?,
    ): KakaoAddressResponse

    // 지도에 표시할 카페 검색하기
    @GET("/v2/local/search/category.json?category_group_code=CE7&sort=distance")
    suspend fun getSearchMapCafes(
        @Query("x") longitude: String,
        @Query("y") latitude: String,
        @Query("page") page: Int,
        @Query("rect") rect: String
    ): KakaoSearchPlaceResponse

    // 네이버 이미지 검색하기
    @GET("v1/search/image?display=1")
    suspend fun getSearchImage(
        @Query("query") query: String,
    ): NaverSearchImageResponse

    // 방문 인증 정보 저장
    @Multipart
    @POST("insertVisitInfo.php")
    fun postVisitInfo(
        @Part("data") data: RequestBody,
        @Part img: MultipartBody.Part?
    ): Call<MyResponse<PointUpdateResponse>>

    // 방문 카페 리스트 조회
    @GET("loadVisitedCafeList.php")
    fun getVisitedCafeList(
        @Query("user_id") user_id: Int,
        @Query("filter") filter: String = ""
    ): Call<MyResponse<List<VisitedCafeItem>>>

    // 해시태그 리스트 조회
    @GET("loadHashtagNames.php")
    fun getHastTagNames(
        @Query("filter") filter: String = ""
    ): Call<MyResponse<List<HashTagItem>>>

    // 리뷰 정보 저장
    @Multipart
    @POST("insertReviewInfo.php")
    fun postReviewAdd(
        @Part("data") data: RequestBody,
        @Part imgs: List<MultipartBody.Part?>
    ): Call<MyResponse<PointUpdateResponse>>

    // 리뷰 리스트 요청
    @GET("loadReviewList.php")
    fun getReviewList(
        @Query("query") query: String,
        @Query("user_id") user_id: Int?=null,
    ): Call<MyResponse<List<ReviewListItemResponse>>>

    // 리뷰 상세 정보 요청
    @POST("loadReviewDetail.php")
    fun getReviewDetail(
        @Body data: ReviewDetailRequest
    ): Call<MyResponse<ReviewDetailResponse>>

    // 각 카페의 DB 정보들 요청
    @POST("loadHomeCafeList.php")
    suspend fun getHomeCafeList(
        @Body data: HomeCafeRequest
    ): MyResponse<List<HomeCafeResponse>>

    // DB에 저장되어 있는 카페 정보들 중 검색어, 카테고리, 해시태그에 맞는 카페의 id들 요청
    @POST("loadHomeCafeFiltering.php")
    fun getHomeCafeFiltering(
        @Body data: HomeHashtagFilteringRequest
    ): Call<MyResponse<HomeHashtagFilteringResponse>> // place_id 리스트

    @GET("v2/local/search/keyword.json?category_group_code=CE7")
    suspend fun getSearchCafe(
        @Query("query") query: String,
        @Query("x") longitude: String?= null,
        @Query("y") latitude: String?= null,
        @Query("size") size: Int
    ): KakaoSearchPlaceResponse

    // 좋아요/취소 요청
    @POST("updateReviewLike.php")
    fun updateReviewLike(
        @Body data: ReviewLikeRequest
    ): Call<MyResponse<ReviewLikeResponse>>

}