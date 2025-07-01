package com.yrlee.tpcafelog.model

import com.google.gson.annotations.SerializedName

data class KakaoAddressResponse(
    var meta: AddressMeta,
    var documents: List<MyAddressResponse>
)

data class AddressMeta(
    var total_count: Int,
)

data class MyAddressResponse(
    var address: MyAddress
)

data class MyAddress(
    var address_name: String,
    var region_1depth_name: String,
    var region_2depth_name: String,
    var region_3depth_name: String,
)
