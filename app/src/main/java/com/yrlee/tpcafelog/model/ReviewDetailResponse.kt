package com.yrlee.tpcafelog.model

data class ReviewDetailResponse(
    var review_id: Int,
    var content: String,
    var rating: Float,
    var hashtag: String,
    var like_cnt: Int,
    var images: List<String>,
    var is_like: Boolean,
    var update_at: String,
    var is_modify: Boolean,
    var userInfo: ReviewDetailUser,
    var cafeInfo: CafeItem,
)

data class ReviewDetailUser(
    val user_id: Int,
    val user_name: String,
    val user_img_url: String,
    val user_title: String,
    val user_level: String,
)
