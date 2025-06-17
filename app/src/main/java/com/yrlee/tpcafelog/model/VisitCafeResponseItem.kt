package com.yrlee.tpcafelog.model

import com.google.gson.annotations.SerializedName

data class VisitCafeResponseItem(
    var visit_id : Int,
    var user_id: Int,
    var place_id: String,
    var place_name: String,
    var place_address: String,
    var visited_at: String,
    var is_reviewed: Boolean,
    var img_url: String,
)
