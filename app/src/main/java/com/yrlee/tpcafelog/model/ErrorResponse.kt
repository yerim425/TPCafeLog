package com.yrlee.tpcafelog.model

data class ErrorResponse(
    var status: Int,
    var data: ErrorData,
)

data class ErrorData(
    var message: String,
)


