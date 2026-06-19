package com.example.inspixmobile.domain.contract.repository

import android.content.Context
import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData
import com.example.inspixmobile.data.source.remote.dto.CollectionMeta
import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.model.Image

interface ICollectionRepository {
    fun getCollectionsPaging(
        userUuid: String? = null,
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>>

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

    fun getRecommendedCollections(): Flow<List<List<Collection>>>

    fun getOwnedCollectionsCount(uuid: String): Flow<Int>

    fun getLikedCollectionsCount(uuid: String): Flow<Int>

    suspend fun uploadCollection(
        context: Context,
        title: String,
        description: String,
        selectedTopicId: Int,
        images: List<Image>,
        onProgress: (Float) -> Unit
    ): Response<CollectionResponseDto, Unit>
}