package com.example.inspixmobile.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.inspixmobile.domain.contract.repository.IImageRepository
import com.example.inspixmobile.domain.model.Image
import com.example.inspixmobile.presentation.state.AspectRatioMode
import com.example.inspixmobile.presentation.state.UploadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UploadViewModel(
    private val imageRepository: IImageRepository
) : ViewModel() {

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
}