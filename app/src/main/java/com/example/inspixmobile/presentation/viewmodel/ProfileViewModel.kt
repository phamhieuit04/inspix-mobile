package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
import com.example.inspixmobile.domain.contract.repository.IUserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: IUserRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository
) : ViewModel() {

    private val _userUuid = MutableStateFlow<String?>(null)

    val isRefreshing = MutableStateFlow(false)

    val interactions = collectionInteractionRepository.interactions

    fun setUserUuid(uuid: String) {
        if (_userUuid.value == uuid) return
        _userUuid.value = uuid
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val user = _userUuid
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
    val ownedCollections = _userUuid
        .filterNotNull()
        .flatMapLatest { uuid ->
            userRepository.observeOwnedCollections(uuid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val likedCollections = _userUuid
        .filterNotNull()
        .flatMapLatest { uuid ->
            userRepository.observeLikedCollections(uuid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            userRepository.refreshProfile("${_userUuid.value}")
            isRefreshing.value = false
        }
    }

    fun toggleLike(collectionUuid: String) {
        viewModelScope.launch {
            collectionInteractionRepository.toggleLike(collectionUuid)
        }
    }
}