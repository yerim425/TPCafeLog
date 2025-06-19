package com.yrlee.tpcafelog.model

data class HomeCafeRequest(
    var user_id: Int,
    var place_ids: List<String>
)
