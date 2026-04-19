package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.Flow

class HomeViewModel(
    private val collectionRepository: ICollectionRepository
) : ViewModel() {

    fun getCollectionsPaging(
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>> {
        return collectionRepository
            .getCollectionsPaging(pageSize = pageSize, prefetchDistance = prefetchDistance)
            .cachedIn(viewModelScope)
    }
}