package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val collectionRepository: ICollectionRepository,
    private val topicRepository: ITopicRepository
) : ViewModel() {

    fun getCollectionsPaging(
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>> {
        return collectionRepository
            .getCollectionsPaging(pageSize = pageSize, prefetchDistance = prefetchDistance)
            .cachedIn(viewModelScope)
    }

    fun getTopics(): StateFlow<List<Topic>> {
        val topics = topicRepository.getTopics()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
        return topics
    }

    fun refreshTopics() {
        viewModelScope.launch {
            topicRepository.refreshTopics()
        }
    }
}