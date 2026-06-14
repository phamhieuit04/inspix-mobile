package com.example.inspixmobile.presentation.state

sealed interface DownloadState {
    object Idle : DownloadState
    data class Downloading(val progress: Float) : DownloadState
    object Done : DownloadState
    object Error : DownloadState
}