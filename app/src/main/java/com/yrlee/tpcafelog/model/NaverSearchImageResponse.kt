package com.yrlee.tpcafelog.model

data class NaverSearchImageResponse(
    var items: List<NaverImage>
)

data class NaverImage(
    var title: String,
    var link: String
)
