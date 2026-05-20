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
import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.local.dao.ImageDao
import com.example.inspixmobile.data.source.local.dao.RemoteKeyDao
import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.data.source.local.db.AppDatabase
import com.example.inspixmobile.data.source.local.entity.RemoteKeyEntity
import com.example.inspixmobile.data.source.local.relationship.CollectionWithImagesAndAuthor
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
    private val userDao: UserDao,
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
                initialLoadSize = pageSize * 2,
                prefetchDistance = prefetchDistance,
                enablePlaceholders = false
            ),
            remoteMediator = CollectionRemoteMediator(
                database = database,
                collectionDao = collectionDao,
                imageDao = imageDao,
                userDao = userDao,
                remoteKeyDao = remoteKeyDao,
                pageSize = pageSize,
                fetchPage = { limit, offset -> fetchRemoteCollections(limit, offset, null) }
            ),
            pagingSourceFactory = { collectionDao.getPagingCollectionsWithImages() }
        ).flow.map { pagingData ->
            pagingData.map { relation -> relation.toDomain() }
        }
    }

    override fun getCollectionsPagingByTopic(
        topicId: Int,
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
            pagingSourceFactory = {
                TopicCollectionsPagingSource(
                    pageSize = pageSize,
                    fetchPage = { limit, offset ->
                        fetchRemoteCollections(limit, offset, topicId)
                    }
                )
            }
        ).flow
    }

    private suspend fun fetchRemoteCollections(
        limit: Int,
        offset: Int,
        topicId: Int?
    ): Response<CollectionResponseDto> {
        val body = client.get("v1/collections/random") {
            parameter("limit", limit)
            parameter("offset", offset)

            topicId?.takeIf { it > 0 }?.let {
                parameter("topic_id", it)
            }
        }.bodyAsText()

        return json.decodeFromString(body)
    }
}

@OptIn(ExperimentalPagingApi::class)
private class CollectionRemoteMediator(
    private val database: AppDatabase,
    private val collectionDao: CollectionDao,
    private val imageDao: ImageDao,
    private val userDao: UserDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val pageSize: Int,
    private val fetchPage: suspend (limit: Int, offset: Int) -> Response<CollectionResponseDto>
) : RemoteMediator<Int, CollectionWithImagesAndAuthor>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CollectionWithImagesAndAuthor>
    ): MediatorResult {
        return try {
            val currentRemoteKey = remoteKeyDao.getByLabel(COLLECTIONS_REMOTE_KEY_LABEL)
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> currentRemoteKey?.nextOffset
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            val response = fetchPage(pageSize, offset)
            if (response.success != true) {
                return MediatorResult.Error(
                    IllegalStateException(response.message ?: "Server returned error")
                )
            }

            val remoteCollections = response.data?.items.orEmpty().map { it.toDomain() }

            val collectionEntities = remoteCollections.map { it.toEntity() }
            val imageEntities = remoteCollections.flatMap { collection ->
                collection.images.orEmpty().map { image ->
                    image.copy(collectionUuid = image.collectionUuid ?: collection.uuid).toEntity()
                }
            }
            val userEntities = remoteCollections
                .mapNotNull { it.author }
                .map { it.toEntity() }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    imageDao.clearAll()
                    collectionDao.clearAll()
                    userDao.clearAll()
                    remoteKeyDao.deleteByLabel(COLLECTIONS_REMOTE_KEY_LABEL)
                }
                userDao.insertAll(userEntities)
                collectionDao.insertAll(collectionEntities)
                imageDao.insertAll(imageEntities)

                val nextOffset = offset + remoteCollections.size
                remoteKeyDao.insert(
                    RemoteKeyEntity(
                        label = COLLECTIONS_REMOTE_KEY_LABEL,
                        nextOffset = nextOffset
                    )
                )
            }

            val endOfPaginationReached =
                remoteCollections.isEmpty() || remoteCollections.size < pageSize

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            Log.e("CollectionRepository", "Failed to load page", e)
            MediatorResult.Error(e)
        }
    }
}

private class TopicCollectionsPagingSource(
    private val pageSize: Int,
    private val fetchPage: suspend (limit: Int, offset: Int) -> Response<CollectionResponseDto>
) : PagingSource<Int, Collection>() {

    override fun getRefreshKey(state: PagingState<Int, Collection>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val closestPage = state.closestPageToPosition(anchorPosition) ?: return null
        return closestPage.prevKey?.let { it + pageSize }
            ?: closestPage.nextKey?.let { it - pageSize }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Collection> {
        val offset = params.key ?: 0
        return try {
            val response = fetchPage(params.loadSize, offset)
            if (response.success != true) {
                return LoadResult.Error(
                    IllegalStateException(response.message ?: "Server returned error")
                )
            }

            val items = response.data?.items.orEmpty().map { it.toDomain() }
            val nextKey = if (items.isEmpty()) null else offset + items.size
            val prevKey = if (offset == 0) null else maxOf(0, offset - params.loadSize)

            LoadResult.Page(
                data = items,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
