package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.IUserInteractionRepository
import com.example.inspixmobile.domain.contract.repository.IUserRepository
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.presentation.state.InteractionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: IUserRepository,
    private val collectionRepository: ICollectionRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository,
    private val userInteractionRepository: IUserInteractionRepository
) : ViewModel() {

    private val _uuid = MutableStateFlow<String?>(null)

    val isRefreshing = MutableStateFlow(false)

    val interactions = collectionInteractionRepository.interactions

    fun setUserUuid(uuid: String) {
        if (_uuid.value == uuid) return
        _uuid.value = uuid
        refresh()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val user = _uuid
        .filterNotNull()
        .flatMapLatest { uuid ->
            userRepository.findProfile(uuid)
        }
        .map { user ->
            user?.let { userInteractionRepository.seed(it) }
            user
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val ownedCollections = _uuid
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val likedCollections = _uuid
        .filterNotNull()
        .flatMapLatest { uuid ->
            userRepository.getLikedCollectionsPager(
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

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 10
        private const val DEFAULT_PREFETCH_DISTANCE = 5
    }
}