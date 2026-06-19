package com.example.inspixmobile.presentation.state

import com.example.inspixmobile.domain.model.Image

enum class AspectRatioMode(val label: String, val ratio: Float?) {
    RATIO_3_4("3:4", 3f / 4f),
    RATIO_9_16("9:16", 9f / 16f),
    RATIO_1_1("1:1", 1f),
    FULL("Full", null)
}

data class UploadUiState(
    val images: List<Image> = emptyList(),
    val showGrid: Boolean = false,
    val flashEnabled: Boolean = false,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.RATIO_3_4,
    val isFrontCamera: Boolean = false
)

sealed interface UploadState {
    object Idle : UploadState
    data class Uploading(val progress: Float) : UploadState
    object Done : UploadState
    object Error : UploadState
}