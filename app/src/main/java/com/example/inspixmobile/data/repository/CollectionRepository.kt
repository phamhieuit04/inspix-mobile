package com.example.inspixmobile.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.map
import androidx.room.withTransaction
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.local.dao.ImageDao
import com.example.inspixmobile.data.source.local.dao.RemoteKeyDao
import com.example.inspixmobile.data.source.local.db.AppDatabase
import com.example.inspixmobile.data.source.local.entity.RemoteKeyEntity
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

private const val COLLECTIONS_REMOTE_KEY_LABEL = "collections"

class CollectionRepository(
    private val database: AppDatabase,
    private val collectionDao: CollectionDao,
    private val imageDao: ImageDao,
    private val remoteKeyDao: RemoteKeyDao,
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
                remoteKeyDao = remoteKeyDao,
                pageSize = pageSize,
                fetchPage = ::fetchRemoteCollections
            ),
            pagingSourceFactory = { collectionDao.getPagingCollectionsWithImages() }
        ).flow.map { pagingData ->
            pagingData.map { relation -> relation.toDomain() }
        }
    }

    private suspend fun fetchRemoteCollections(
        limit: Int,
        offset: Int
    ): Response<CollectionResponseDto> {
        val body = client.get("v1/collections/random") {
            parameter("limit", limit)
            parameter("offset", offset)
        }.bodyAsText()
        return json.decodeFromString<Response<CollectionResponseDto>>(body)
    }
}

@OptIn(ExperimentalPagingApi::class)
private class CollectionRemoteMediator(
    private val database: AppDatabase,
    private val collectionDao: CollectionDao,
    private val imageDao: ImageDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val pageSize: Int,
    private val fetchPage: suspend (limit: Int, offset: Int) -> Response<CollectionResponseDto>
) : RemoteMediator<Int, CollectionWithImages>() {

    override suspend fun initialize(): InitializeAction {
        // If local cache exists, keep first render stable and avoid eager network refresh loops.
        return if (collectionDao.countCollections() > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CollectionWithImages>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = remoteKeyDao.getByLabel(COLLECTIONS_REMOTE_KEY_LABEL)
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    remoteKey.nextOffset
                }
            }

            val response = fetchPage(pageSize, offset)
            val remoteCollections = response.data?.items.orEmpty().map { it.toDomain() }
            val collectionEntities = remoteCollections.map { it.toEntity() }
            val imageEntities = remoteCollections.flatMap { collection ->
                collection.images.orEmpty().map { image ->
                    image.copy(collectionUuid = image.collectionUuid ?: collection.uuid).toEntity()
                }
            }

            val localCountBefore = if (loadType == LoadType.APPEND) {
                collectionDao.countCollections()
            } else {
                0
            }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    imageDao.clearAll()
                    collectionDao.clearAll()
                    remoteKeyDao.deleteByLabel(COLLECTIONS_REMOTE_KEY_LABEL)
                }
                collectionDao.insertAll(collectionEntities)
                imageDao.insertAll(imageEntities)
            }

            val localCountAfter = if (loadType == LoadType.APPEND) {
                collectionDao.countCollections()
            } else {
                0
            }
            val insertedCount = if (loadType == LoadType.APPEND) {
                localCountAfter - localCountBefore
            } else {
                remoteCollections.size
            }
            val noProgressOnAppend = loadType == LoadType.APPEND && insertedCount <= 0

            val nextOffset = offset + remoteCollections.size
            val endOfPaginationReached =
                remoteCollections.isEmpty() ||
                    remoteCollections.size < pageSize ||
                    noProgressOnAppend

            if (!endOfPaginationReached && remoteCollections.isNotEmpty()) {
                remoteKeyDao.insert(
                    RemoteKeyEntity(
                        label = COLLECTIONS_REMOTE_KEY_LABEL,
                        nextOffset = nextOffset
                    )
                )
            } else {
                // Clear key to avoid re-requesting the same offset indefinitely.
                remoteKeyDao.deleteByLabel(COLLECTIONS_REMOTE_KEY_LABEL)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            Log.e("CollectionRepository", "Failed to load page", e)
            MediatorResult.Error(e)
        }
    }
}