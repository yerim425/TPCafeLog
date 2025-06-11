package com.yrlee.tpcafelog.model

data class User(
    val kakao_id: String,
    val name: String,
    val profile_img_url: String,
    val level: Int,
    val created_at: String
)
