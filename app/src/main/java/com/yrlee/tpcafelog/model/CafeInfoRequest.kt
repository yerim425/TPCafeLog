package com.yrlee.tpcafelog.model

data class CafeInfoRequest(
    var user_id: Int,
    var place_ids: List<String>
)
