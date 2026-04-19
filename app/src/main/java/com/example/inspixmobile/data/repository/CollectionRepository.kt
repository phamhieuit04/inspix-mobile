package com.example.inspixmobile.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import androidx.paging.map
import androidx.room.withTransaction
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.local.dao.ImageDao
import com.example.inspixmobile.data.source.local.db.AppDatabase
import com.example.inspixmobile.data.source.local.relationship.CollectionWithImages
import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.model.Collection
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class CollectionRepository(
    private val database: AppDatabase,
    private val collectionDao: CollectionDao,
    private val imageDao: ImageDao,
    private val client: HttpClient,
    private val json: Json
) : ICollectionRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getCollectionsPaging(
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                prefetchDistance = prefetchDistance,
                enablePlaceholders = false
            ),
            remoteMediator = CollectionRemoteMediator(
                database = database,
                collectionDao = collectionDao,
                imageDao = imageDao,
                pageSize = pageSize,
                fetchPage = ::fetchRemoteCollections
            ),
            pagingSourceFactory = { collectionDao.getPagingCollectionsWithImages() }
        ).flow.map { pagingData ->
            pagingData.map { relation -> relation.toDomain() }
        }
    }

    private suspend fun fetchRemoteCollections(limit: Int, offset: Int): CollectionResponseDto {
        val body = client.get("v1/images/random") {
            parameter("limit", limit)
            parameter("offset", offset)
        }.bodyAsText()

        val response = json.decodeFromString<Response<CollectionResponseDto>>(body)
        return response.data ?: CollectionResponseDto(items = emptyList())
    }
}

@OptIn(ExperimentalPagingApi::class)
private class CollectionRemoteMediator(
    private val database: AppDatabase,
    private val collectionDao: CollectionDao,
    private val imageDao: ImageDao,
    private val pageSize: Int,
    private val fetchPage: suspend (limit: Int, offset: Int) -> CollectionResponseDto
) : RemoteMediator<Int, CollectionWithImages>() {

    override suspend fun load(
        loadType: LoadType,
        state: androidx.paging.PagingState<Int, CollectionWithImages>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> collectionDao.countCollections()
            }

            val response = fetchPage(pageSize, offset)
            val remoteCollections = response.items.orEmpty().map { it.toDomain() }
            val collectionEntities = remoteCollections.map { it.toEntity() }
            val imageEntities = remoteCollections.flatMap { collection ->
                collection.images.orEmpty().map { image ->
                    image.copy(collectionId = image.collectionId ?: collection.id).toEntity()
                }
            }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    imageDao.clearAll()
                    collectionDao.clearAll()
                }
                collectionDao.insertAll(collectionEntities)
                imageDao.insertAll(imageEntities)
            }

            val endOfPaginationReached =
                remoteCollections.isEmpty() || response.meta?.has_more == false

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            Log.e("CollectionRepository", "Failed to load page", e)
            MediatorResult.Error(e)
        }
    }
}