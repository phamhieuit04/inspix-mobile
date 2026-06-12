package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.IUserInteractionRepository
import com.example.inspixmobile.domain.contract.repository.IUserRepository
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FollowingViewModel(
    private val userRepository: IUserRepository,
    private val collectionRepository: ICollectionRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository,
    private val userInteractionRepository: IUserInteractionRepository
) : ViewModel() {

    val collectionInteractions = collectionInteractionRepository.interactions
    val userInteractions = userInteractionRepository.interactions

    val followedCollections = userRepository
        .getFollowedCollectionsPaging(
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

    val recommendedCollections = collectionRepository
        .getRecommendedCollections()
        .map { groups ->
            groups.map { collections ->
                collections.shuffled().forEach { collection ->
                    collectionInteractionRepository.seed(collection)
                    collection.author?.let { userInteractionRepository.seed(it) }
                }
                collections
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )


    fun toggleLike(collection: Collection) {
        viewModelScope.launch {
            collectionInteractionRepository.toggleLike(collection)
        }
    }

    fun toggleFollow(user: User) {
        viewModelScope.launch {
            userInteractionRepository.toggleFollow(user)
        }
    }

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 5
        private const val DEFAULT_PREFETCH_DISTANCE = 2
    }
}