package com.yrlee.tpcafelog.model

data class MyResponse<T>(
    var status: Int,
    var data: T? = null
)


