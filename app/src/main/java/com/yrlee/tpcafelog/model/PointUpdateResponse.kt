package com.yrlee.tpcafelog.model

data class PointUpdateResponse(
    var user_id: Int,
    var user_level: Int,
    var user_point: Int,
    var gained_point: Int,
    var level_up: Boolean
)
