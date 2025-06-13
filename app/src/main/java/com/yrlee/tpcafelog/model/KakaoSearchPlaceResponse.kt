package com.yrlee.tpcafelog.model

import com.google.gson.annotations.SerializedName

data class KakaoSearchPlaceResponse(
    var meta: PlaceMeta,
    var documents: List<Place>
)

data class PlaceMeta(
    var total_count: Int,
    var pageable_count: Int,
    var is_end: Boolean
)

data class Place(
    var id: String,
    var place_name: String,
    var category_name: String,
    var phone: String,
    var address_name: String,
    var road_address_name: String,
    @SerializedName("x") var longitude: String,
    @SerializedName("y") var latitude: String,
    var place_url: String,
    var distance: String,
    var img_url: String? = null,
    var isImgRequested: Boolean = false
)
