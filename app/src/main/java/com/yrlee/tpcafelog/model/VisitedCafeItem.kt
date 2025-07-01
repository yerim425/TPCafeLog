package com.yrlee.tpcafelog.model

data class VisitedCafeItem(
    var visit_id : Int,
    var place_id: String,
    var place_name: String,
    var place_address: String,
    var visited_at: String,
    var is_reviewed: Boolean,
    var img_url: String,
)
