package com.yrlee.tpcafelog.model

data class User(
    val kakao_id: String,
    val name: String,
    val level: Int,
    val points: Int,
    val created_at: String
)
