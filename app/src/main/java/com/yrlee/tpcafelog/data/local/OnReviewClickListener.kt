package com.yrlee.tpcafelog.data.local

import com.yrlee.tpcafelog.model.ReviewListItemResponse

interface OnReviewClickListener {
    fun onItemClick(item: ReviewListItemResponse)

    fun onLikeClick(item: ReviewListItemResponse, isChecked: Boolean)
}