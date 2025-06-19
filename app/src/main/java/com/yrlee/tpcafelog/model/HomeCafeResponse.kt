package com.yrlee.tpcafelog.model

data class HomeCafeResponse(
    var place_id: String,
    var is_like: Boolean,
    var avg_rating: Float?=null,
    var visit_id: Int = 0,
    var review_id: Int = 0,
    var visit_datas: List<HomeVisit>?=null,
    var hashtag_names: String?=null
)

data class HomeVisit(
    var visit_id: Int,
    var visit_img_url: String,
    var is_reviewed: Boolean,
)