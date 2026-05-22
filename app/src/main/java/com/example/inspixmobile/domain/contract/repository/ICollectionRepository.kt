package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

interface ICollectionRepository {
    fun getCollectionsPaging(pageSize: Int, prefetchDistance: Int): Flow<PagingData<Collection>>

    fun getCollectionsPagingByTopic(
        topicId: Int,
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>>

    fun getCollectionByUuid(uuid: String): Flow<Collection>
}