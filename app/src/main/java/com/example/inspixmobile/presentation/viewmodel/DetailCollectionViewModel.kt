package com.example.inspixmobile.presentation.viewmodel

import android.content.Context
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
import com.example.inspixmobile.domain.contract.repository.IImageRepository
import com.example.inspixmobile.domain.contract.repository.IUserInteractionRepository
import com.example.inspixmobile.domain.model.Image

class DetailCollectionViewModel(
    private val collectionRepository: ICollectionRepository,
    private val imageRepository: IImageRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository,
    private val userInteractionRepository: IUserInteractionRepository
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
                        collection.author?.let { userInteractionRepository.seed(it) }

                        collection
                    }
                }
                .cachedIn(viewModelScope)
        }
    }

    fun toggleLike(collection: Collection) {
        viewModelScope.launch {
            collectionInteractionRepository.toggleLike(collection)
        }
    }

    fun downloadImage(context: Context, image: Image) {
        viewModelScope.launch {
            imageRepository.download(image = image, context = context)
        }
    }

    private companion object {
        private const val DEFAULT_PAGE_SIZE = 30
        private const val DEFAULT_PREFETCH_DISTANCE = 20
    }
}