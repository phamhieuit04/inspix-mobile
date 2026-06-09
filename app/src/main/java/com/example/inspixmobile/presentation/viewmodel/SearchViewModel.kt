package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.ITopicRepository
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SearchViewModel(
    private val topicRepository: ITopicRepository,
    private val collectionRepository: ICollectionRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository
) : ViewModel() {

    private val cachedTopics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = cachedTopics

    val interactions = collectionInteractionRepository.interactions

    init {
        viewModelScope.launch {
            topicRepository.getTopics()
                .map { list -> list.shuffled() }
                .collect { list ->
                    if (cachedTopics.value.isEmpty() && list.isNotEmpty()) {
                        cachedTopics.value = list
                    }
                }
        }
    }

    private val topicCollectionsCache = mutableMapOf<Int, Flow<PagingData<Collection>>>()
    private val queryCollectionsCache = mutableMapOf<String, Flow<PagingData<Collection>>>()

    fun getCollectionsPagingByTopic(topicId: Int): Flow<PagingData<Collection>> {
        return topicCollectionsCache.getOrPut(topicId) {
            collectionRepository
                .getCollectionsPagingByTopic(
                    topicId = topicId,
                    pageSize = DEFAULT_PAGE_SIZE,
                    prefetchDistance = DEFAULT_PREFETCH_DISTANCE
                )
                .map { pagingData ->
                    pagingData.map { collection ->
                        collectionInteractionRepository.seed(collection)
                        collection
                    }
                }.cachedIn(viewModelScope)
        }
    }

    fun getCollectionsPagingByQuery(query: String): Flow<PagingData<Collection>> {
        val normalizedQuery = query.trim()
        return queryCollectionsCache.getOrPut(normalizedQuery) {
            collectionRepository
                .getCollectionsByQuery(
                    query = normalizedQuery,
                    pageSize = DEFAULT_PAGE_SIZE,
                    prefetchDistance = DEFAULT_PREFETCH_DISTANCE
                )
                .map { pagingData ->
                    pagingData.map { collection ->
                        collectionInteractionRepository.seed(collection)
                        collection
                    }
                }.cachedIn(viewModelScope)
        }
    }

    fun toggleLike(collectionUuid: String) {
        viewModelScope.launch {
            collectionInteractionRepository.toggleLike(collectionUuid)
        }
    }

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 30
        private const val DEFAULT_PREFETCH_DISTANCE = 10
    }
}