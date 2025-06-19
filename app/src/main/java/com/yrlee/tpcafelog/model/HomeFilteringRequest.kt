package com.yrlee.tpcafelog.model


data class HomeFilteringRequest(
    var query:String?=null,
    var category: String? =null,
    var hashtag_ids: List<Int>
)
