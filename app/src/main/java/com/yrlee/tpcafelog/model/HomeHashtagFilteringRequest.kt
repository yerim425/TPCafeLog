package com.yrlee.tpcafelog.model

data class HomeHashtagFilteringRequest(
    var query: String?=null,
    var category: String?=null,
    var hashtag_ids: List<Int>,
    var page: Int
)
