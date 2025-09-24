package com.yrlee.tpcafelog.model

data class ReviewListRequest(
    val user_id: Int?=null,
    var query : String ?= "",
    val page : Int ?= 0
)
