package com.yrlee.tpcafelog.model

import android.net.Uri

sealed class PhotoItem {
    data class Remote(val url: String) : PhotoItem()
    data class Local(val uri: Uri) : PhotoItem()
}
