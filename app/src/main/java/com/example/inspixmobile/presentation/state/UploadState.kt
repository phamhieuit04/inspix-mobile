package com.example.inspixmobile.presentation.state

import android.net.Uri

data class UploadState(
    val images: List<Uri> = emptyList()
)