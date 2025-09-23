package com.yrlee.tpcafelog.model

data class ReviewLikeRequest(
    val user_id: Int?=null,
    val review_id: Int,
    val is_like: Boolean
)
