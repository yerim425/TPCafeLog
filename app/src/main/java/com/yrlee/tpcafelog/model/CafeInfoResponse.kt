package com.yrlee.tpcafelog.model

data class CafeInfoResponse(
    var place_id: String, // 카페 장소 id
    var is_like: Boolean, // 사용자 좋아요 여부
    var avg_rating: Float?=null, // 리뷰 평균 점수
    var visit_cnt: Int=0,
    var review_cnt: Int=0,
    var visit_datas: List<CafeInfoVisit>?=null, // 방문 인증 리스트
    var hashtag_names: String?=null // 해시태그 이름 리스트
)

data class CafeInfoVisit(
    var visit_id: Int, // 방문 인증 id
    var visit_img_url: String, // 방문 인증 이미지
    var is_reviewed: Boolean, // 리뷰 여부
)