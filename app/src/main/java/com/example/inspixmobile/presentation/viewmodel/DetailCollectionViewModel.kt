package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.ICommentRepository
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.Flow


class DetailCollectionViewModel(
    private val collectionRepository: ICollectionRepository,
    private val commentRepository: ICommentRepository
) : ViewModel() {

    private val cachedFlows = mutableMapOf<String, Flow<PagingData<Collection>>>()

    fun getExploreCollectionsPaging(collectionUuid: String): Flow<PagingData<Collection>> {
        return cachedFlows.getOrPut(collectionUuid) {
            collectionRepository.getExploreCollectionsPaging(
                collectionUuid = collectionUuid,
                pageSize = DEFAULT_PAGE_SIZE,
                prefetchDistance = DEFAULT_PREFETCH_DISTANCE
            ).cachedIn(viewModelScope)
        }
    }

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 30
        private const val DEFAULT_PREFETCH_DISTANCE = 20
    }
}