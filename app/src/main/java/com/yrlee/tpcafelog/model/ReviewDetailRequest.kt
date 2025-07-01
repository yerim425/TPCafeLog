package com.yrlee.tpcafelog.model

data class ReviewDetailRequest(
    val user_id: Int?=null,
    val review_id: Int,
)
