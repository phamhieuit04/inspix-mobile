package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.IUserRepository
import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailArtistViewModel(
    private val userRepository: IUserRepository,
    private val collectionRepository: ICollectionRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository
) : ViewModel() {

    private val _uuid = MutableStateFlow<String?>(null)

    val isRefreshing = MutableStateFlow(false)

    val interactions = collectionInteractionRepository.interactions

    fun setUserUuid(uuid: String) {
        if (_uuid.value == uuid) return
        _uuid.value = uuid
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val user = _uuid
        .filterNotNull()
        .flatMapLatest { uuid ->
            userRepository.findProfile(uuid)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val artistCollections = _uuid
        .filterNotNull()
        .flatMapLatest { uuid ->
            userRepository.getOwnedCollections(
                userUuid = uuid,
                pageSize = DEFAULT_PAGE_SIZE,
                prefetchDistance = DEFAULT_PREFETCH_DISTANCE
            )
        }
        .map { pagingData ->
            pagingData.map { collection ->
                collectionInteractionRepository.seed(collection)
                collection
            }
        }
        .cachedIn(viewModelScope)

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true

            _uuid.value?.let { uuid ->
                userRepository.refreshProfile(
                    uuid = uuid,
                    offset = 0,
                    limit = DEFAULT_PAGE_SIZE
                )
            }

            isRefreshing.value = false
        }
    }

    fun toggleLike(collection: Collection) {
        viewModelScope.launch {
            collectionInteractionRepository.toggleLike(collection)
        }
    }

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 10
        private const val DEFAULT_PREFETCH_DISTANCE = 5
    }
}