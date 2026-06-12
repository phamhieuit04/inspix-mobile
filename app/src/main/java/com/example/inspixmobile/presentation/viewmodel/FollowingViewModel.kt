package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.IUserInteractionRepository
import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FollowingViewModel(
    private val collectionRepository: ICollectionRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository,
    private val userInteractionRepository: IUserInteractionRepository
) : ViewModel() {

    val interactions = collectionInteractionRepository.interactions

    val followedCollections = collectionRepository
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

    fun toggleLike(collection: Collection) {
        viewModelScope.launch {
            collectionInteractionRepository.toggleLike(collection)
        }
    }

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 5
        private const val DEFAULT_PREFETCH_DISTANCE = 2
    }
}