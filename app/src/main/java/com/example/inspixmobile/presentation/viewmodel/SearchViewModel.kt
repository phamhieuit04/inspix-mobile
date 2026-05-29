package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.ITopicRepository
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(
    private val topicRepository: ITopicRepository,
    private val collectionRepository: ICollectionRepository
) : ViewModel() {

    val topics: StateFlow<List<Topic>> = topicRepository.getTopics()
        .map { list -> list.shuffled() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun getCollectionsPagingByTopic(topicId: Int): Flow<PagingData<Collection>> {
        return collectionRepository.getCollectionsPagingByTopic(
            topicId = topicId,
            pageSize = DEFAULT_PAGE_SIZE,
            prefetchDistance = DEFAULT_PREFETCH_DISTANCE
        ).cachedIn(viewModelScope)
    }

    fun getCollectionsPagingByQuery(query: String): Flow<PagingData<Collection>> {
        return collectionRepository.getCollectionsByQuery(
            query = query,
            pageSize = DEFAULT_PAGE_SIZE,
            prefetchDistance = DEFAULT_PREFETCH_DISTANCE
        ).cachedIn(viewModelScope)
    }

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 30
        private const val DEFAULT_PREFETCH_DISTANCE = 10
    }
}