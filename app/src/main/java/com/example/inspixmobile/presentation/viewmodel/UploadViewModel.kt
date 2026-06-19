package com.example.inspixmobile.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.data.source.local.store.SessionStore
import com.example.inspixmobile.domain.contract.repository.IImageRepository
import com.example.inspixmobile.domain.contract.repository.ITopicRepository
import com.example.inspixmobile.domain.model.Image
import com.example.inspixmobile.domain.model.Topic
import com.example.inspixmobile.presentation.state.AspectRatioMode
import com.example.inspixmobile.presentation.state.DownloadState
import com.example.inspixmobile.presentation.state.UploadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UploadViewModel(
    private val imageRepository: IImageRepository,
    private val topicRepository: ITopicRepository
) : ViewModel() {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState = _downloadState.asStateFlow()

    private val cachedTopics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = cachedTopics

    init {
        viewModelScope.launch {
            topicRepository.getTopics()
                .collect { list ->
                    if (list.isNotEmpty()) {
                        cachedTopics.value = list
                    }
                }
        }
    }

    private val _uiState = MutableStateFlow(UploadState())
    val uiState = _uiState.asStateFlow()

    fun addImage(image: Image) {
        _uiState.update { state ->
            state.copy(images = (state.images + image).distinct())
        }
    }

    fun removeImage(image: Image) {
        _uiState.update { state ->
            state.copy(images = state.images - image)
        }
    }

    fun removeImageAt(index: Int) {
        _uiState.update { state ->
            if (index !in state.images.indices) return@update state
            state.copy(
                images = state.images.toMutableList().apply { removeAt(index) }
            )
        }
    }

    fun replaceImages(images: List<Image>) {
        _uiState.update { state ->
            state.copy(images = images.distinct())
        }
    }

    fun clearImages() {
        _uiState.update { state ->
            state.copy(images = emptyList())
        }
    }

    fun toggleGrid() {
        _uiState.update { state ->
            state.copy(showGrid = !state.showGrid)
        }
    }

    fun toggleFlash() {
        _uiState.update { state ->
            state.copy(flashEnabled = !state.flashEnabled)
        }
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        _uiState.update { state ->
            state.copy(aspectRatioMode = mode)
        }
    }

    fun toggleCamera() {
        _uiState.update { state ->
            state.copy(isFrontCamera = !state.isFrontCamera)
        }
    }

    private var downloadJob: Job? = null

    fun downloadImage(context: Context, image: Image) {
        downloadJob = viewModelScope.launch {
            _downloadState.value = DownloadState.Downloading(progress = -1f)

            val result = imageRepository.saveCapturedPhoto(
                context = context,
                uri = image.uri.toString(),
            )

            _downloadState.value = if (result.isSuccess) DownloadState.Done else DownloadState.Error
        }

        downloadJob?.invokeOnCompletion { cause ->
            if (cause != null) {
                _downloadState.value = DownloadState.Idle
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _downloadState.value = DownloadState.Idle
    }

    fun dismissDownloadDialog() {
        _downloadState.value = DownloadState.Idle
    }
}