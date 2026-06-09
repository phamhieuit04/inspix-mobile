package com.example.inspixmobile.presentation.viewmodel

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

class DetailCollectionViewModel(
    private val collectionRepository: ICollectionRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository
) : ViewModel() {

    private val cachedFlows = mutableMapOf<String, Flow<PagingData<Collection>>>()

    val interactions = collectionInteractionRepository.interactions

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
                        collection
                    }
                }
                .cachedIn(viewModelScope)
        }
    }

    fun toggleLike(collectionUuid: String) {
        viewModelScope.launch {
            collectionInteractionRepository.toggleLike(collectionUuid)
        }
    }

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 30
        private const val DEFAULT_PREFETCH_DISTANCE = 20
    }
}