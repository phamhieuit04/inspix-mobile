package com.example.inspixmobile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.paging.map
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.data.source.local.store.SessionStore
import com.example.inspixmobile.domain.contract.repository.IImageRepository
import com.example.inspixmobile.domain.contract.repository.IUserInteractionRepository
import com.example.inspixmobile.domain.model.Image
import com.example.inspixmobile.presentation.state.DownloadState
import com.example.inspixmobile.presentation.state.InteractionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class DetailCollectionViewModel(
    private val sessionStore: SessionStore,
    private val collectionRepository: ICollectionRepository,
    private val imageRepository: IImageRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository,
    private val userInteractionRepository: IUserInteractionRepository
) : ViewModel() {

    private val cachedFlows = mutableMapOf<String, Flow<PagingData<Collection>>>()

    val interactions = collectionInteractionRepository.interactions

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState = _downloadState.asStateFlow()

    private var downloadJob: Job? = null

    fun getExploreCollectionsPaging(collectionUuid: String): Flow<PagingData<Collection>> {
        return cachedFlows.getOrPut(collectionUuid) {
            collectionRepository
                .getExploreCollectionsPaging(
                    collectionUuid = collectionUuid,
                    pageSize = DEFAULT_PAGE_SIZE,
                    prefetchDistance = DEFAULT_PREFETCH_DISTANCE
                )
                .map { pagingData ->
                    pagingData.map { collection ->
                        collectionInteractionRepository.seed(collection)
                        collection.author?.let { userInteractionRepository.seed(it) }

                        collection
                    }
                }
                .cachedIn(viewModelScope)
        }
    }

    fun toggleLike(collection: Collection) {
        viewModelScope.launch {
            val session = sessionStore.session.first()
            if (!session.isLoggedIn) {
                EventBus.emit(Event.RequireSignIn)
                return@launch
            }

            val result = collectionInteractionRepository.toggleLike(collection)

            when (result) {
                is InteractionState.Success -> {}
                is InteractionState.Unauthorized -> {
                    EventBus.emit(Event.RequireSignIn)
                }

                is InteractionState.Network -> {
                    EventBus.emit(Event.NetworkError)
                }

                is InteractionState.Unknown -> {
                    EventBus.emit((Event.NetworkError))
                }
            }
        }
    }

    fun downloadImage(context: Context, image: Image) {
        downloadJob = viewModelScope.launch {
            val session = sessionStore.session.first()
            if (!session.isLoggedIn) {
                EventBus.emit(Event.RequireSignIn)
                return@launch
            }

            _downloadState.value = DownloadState.Downloading(progress = -1f)

            val result = imageRepository.download(
                context = context,
                image = image,
                onProgress = { progress ->
                    _downloadState.value = DownloadState.Downloading(progress = progress)
                }
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

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 30
        private const val DEFAULT_PREFETCH_DISTANCE = 20
    }
}