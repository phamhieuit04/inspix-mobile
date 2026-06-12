package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData
import com.example.inspixmobile.data.source.remote.dto.CollectionMeta
import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
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

    fun getExploreCollectionsPaging(
        collectionUuid: String,
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>>

    suspend fun fetchExploreCollections(
        collectionUuid: String,
        limit: Int,
        offset: Int
    ): Response<List<CollectionResponseDto>, CollectionMeta>

    fun getCollectionsByQuery(
        query: String,
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>>

    suspend fun fetchCollectionsByQuery(
        query: String,
        offset: Int,
        limit: Int
    ): Response<List<CollectionResponseDto>, CollectionMeta>

    fun getCachedCollections(): Flow<List<Collection>>

    suspend fun fetchFollowedCollections(
        limit: Int,
        offset: Int
    ): Response<List<CollectionResponseDto>, CollectionMeta>

    fun getFollowedCollectionsPaging(
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>>
}