package com.yrlee.tpcafelog.model

data class ReviewItem(
    var user_id: Int,
    var place_id: String,
    var visit_id: Int,
    var content: String,
    var rating: Float,
    var hashtag_ids: List<Int>
)
