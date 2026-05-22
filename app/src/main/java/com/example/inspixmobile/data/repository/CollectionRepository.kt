package com.example.inspixmobile.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.local.dao.ImageDao
import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.data.source.local.db.AppDatabase
import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.model.Collection
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class CollectionRepository(
    private val database: AppDatabase,
    private val collectionDao: CollectionDao,
    private val imageDao: ImageDao,
    private val userDao: UserDao,
    private val client: HttpClient,
    private val json: Json
) : ICollectionRepository {

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
            pagingSourceFactory = {
                AllCollectionsPagingSource(
                    database = database,
                    collectionDao = collectionDao,
                    imageDao = imageDao,
                    userDao = userDao,
                    pageSize = pageSize,
                    fetchPage = { limit, offset -> fetchRemoteCollections(limit, offset, null) }
                )
            }
        ).flow
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

private class AllCollectionsPagingSource(
    private val database: AppDatabase,
    private val collectionDao: CollectionDao,
    private val imageDao: ImageDao,
    private val userDao: UserDao,
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

            if (response.success == false) {
                return loadFromCacheOrError(
                    IllegalStateException(response.message ?: "Server returned error"),
                    offset
                )
            }

            val items = response.data?.items.orEmpty().map { it.toDomain() }
            cacheCollections(items, isRefresh = offset == 0)

            val nextKey = if (items.isEmpty()) null else offset + items.size
            val prevKey = if (offset == 0) null else maxOf(0, offset - params.loadSize)

            LoadResult.Page(
                data = items,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            loadFromCacheOrError(e, offset)
        }
    }

    private suspend fun cacheCollections(
        collections: List<Collection>,
        isRefresh: Boolean
    ) {
        val collectionEntities = collections.map { it.toEntity() }
        val imageEntities = collections.flatMap { collection ->
            collection.images.orEmpty().map { image ->
                image.copy(collectionUuid = image.collectionUuid ?: collection.uuid).toEntity()
            }
        }
        val userEntities = collections
            .mapNotNull { it.author }
            .map { it.toEntity() }

        database.withTransaction {
            if (isRefresh) {
                imageDao.clearAll()
                collectionDao.clearAll()
                userDao.clearAll()
            }
            userDao.insertAll(userEntities)
            collectionDao.insertAll(collectionEntities)
            imageDao.insertAll(imageEntities)
        }
    }

    private suspend fun loadFromCacheOrError(
        throwable: Throwable,
        offset: Int
    ): LoadResult<Int, Collection> {
        if (offset > 0) {
            return LoadResult.Error(throwable)
        }

        val cached = collectionDao.getListCollectionsWithImages()
        if (cached.isEmpty()) {
            return LoadResult.Error(throwable)
        }

        val items = cached.map { it.toDomain() }
        return LoadResult.Page(
            data = items,
            prevKey = null,
            nextKey = null
        )
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
