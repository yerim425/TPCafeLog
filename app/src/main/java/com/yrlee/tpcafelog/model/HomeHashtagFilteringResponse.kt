package com.yrlee.tpcafelog.model

data class HomeHashtagFilteringResponse(
    var querys: List<String>,
    var total_cnt: Int,
    var is_end: Boolean
)
