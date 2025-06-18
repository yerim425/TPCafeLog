package com.yrlee.tpcafelog.model

data class ReviewListItemResponse(
    var review_id: Int,
    var visit_id: Int,
    var rating: Float,
    var content: String,
    var hashtag_names: List<String>,
    var img_url: String,
    var isLike: Boolean?=false,
    var userInfo: ReviewUser,
    var cafeInfo: ReviewCafe,
    var total_cnt: Int,
)

data class ReviewUser(
    var id: Int,
    var name: String,
    var level: Int,
    var title: String,
    var img_url: String,
)

data class ReviewCafe(
    var id: String,
    var name: String,
    var address: String,
    var category: String,
)
