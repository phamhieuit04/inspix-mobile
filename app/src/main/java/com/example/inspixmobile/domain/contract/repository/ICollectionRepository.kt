package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData
import com.example.inspixmobile.data.source.remote.dto.CollectionMeta
import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.remote.dto.ImageResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response

interface ICollectionRepository {
    fun getCollectionsPaging(pageSize: Int, prefetchDistance: Int): Flow<PagingData<Collection>>

    fun getCollectionsPagingByTopic(
        topicId: Int,
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>>

    suspend fun fetchRemoteCollections(
        limit: Int,
        offset: Int,
        topicId: Int?
    ): Response<List<CollectionResponseDto>, CollectionMeta>

    suspend fun fetchExploreCollections(collectionUuid: String): Response<List<CollectionResponseDto>, CollectionMeta>
}