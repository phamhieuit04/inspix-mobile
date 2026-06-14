package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.data.repository.CollectionInteractionRepository
import com.example.inspixmobile.data.source.local.store.SessionStore
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.ITopicRepository
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.Topic
import com.example.inspixmobile.presentation.state.CollectionInteractionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import androidx.paging.map
import com.example.inspixmobile.domain.contract.repository.IUserInteractionRepository
import kotlinx.coroutines.flow.first

class HomeViewModel(
    private val collectionRepository: ICollectionRepository,
    private val topicRepository: ITopicRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository,
    private val userInteractionRepository: IUserInteractionRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val selectedTopicId = MutableStateFlow(0)
    val selectedTopic = selectedTopicId.asStateFlow()

    private val refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    private val loadedTopicIds = MutableStateFlow<Set<Int>>(emptySet())

    val loadedTopics = loadedTopicIds.asStateFlow()

    val topics: StateFlow<List<Topic>> = topicRepository.getTopics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val collectionsCache = mutableMapOf<Int, Flow<PagingData<Collection>>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val collections = combine(selectedTopicId, refreshTrigger) { topicId, _ -> topicId }
        .flatMapLatest { topicId ->
            collectionsCache.getOrPut(topicId) {
                val flow = if (topicId == 0) {
                    val session = sessionStore.session.first()

                    collectionRepository.getCollectionsPaging(
                        userUuid = session.userUuid,
                        pageSize = DEFAULT_PAGE_SIZE,
                        prefetchDistance = DEFAULT_PREFETCH_DISTANCE
                    )
                } else {
                    collectionRepository.getCollectionsPagingByTopic(
                        topicId = topicId,
                        pageSize = DEFAULT_PAGE_SIZE,
                        prefetchDistance = DEFAULT_PREFETCH_DISTANCE
                    )
                }

                flow.map { pagingData ->
                    pagingData.map { collection ->
                        collectionInteractionRepository.seed(collection)
                        collection.author?.let { userInteractionRepository.seed(it) }

                        collection
                    }
                }
                    .cachedIn(viewModelScope)
            }
        }

    val interactions = collectionInteractionRepository.interactions

    init {
        viewModelScope.launch {
            sessionStore.session
                .distinctUntilChangedBy { it.isLoggedIn }
                .drop(1)
                .collect {
                    collectionsCache.clear()
                    collectionInteractionRepository.clear()
                    userInteractionRepository.clear()
                    refreshTrigger.value = System.currentTimeMillis()
                }
        }
    }

    fun refreshTopics() {
        viewModelScope.launch {
            topicRepository.refreshTopics()
        }
    }

    fun markTopicLoaded(topicId: Int) {
        if (loadedTopicIds.value.contains(topicId))
            return
        loadedTopicIds.update {
            it + topicId
        }
    }

    fun toggleLike(collection: Collection) {
        viewModelScope.launch {
            collectionInteractionRepository.toggleLike(collection)
        }
    }

    private companion object {
        const val DEFAULT_PAGE_SIZE = 30
        const val DEFAULT_PREFETCH_DISTANCE = 10
    }
}