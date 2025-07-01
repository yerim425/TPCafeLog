package com.yrlee.tpcafelog.model

data class CafeItem(
    var user_id: Int,
    var place_id: String,
    var name: String,
    var address: String,
    var category: String,
    var lat: String,
    var lng: String,
    var place_url: String,
    var phone: String,
)
