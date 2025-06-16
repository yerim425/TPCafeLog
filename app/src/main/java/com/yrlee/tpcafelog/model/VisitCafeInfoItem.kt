package com.yrlee.tpcafelog.model

data class VisitCafeInfoItem(
    var user_id: Int,
    var visited_id: Int,
    var place_id: String,
    var place_name: String,
    var place_address: String,
    var visited_at: String,
    var img_url: String,
)
