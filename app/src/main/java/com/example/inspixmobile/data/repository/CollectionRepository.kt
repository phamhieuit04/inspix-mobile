package com.example.inspixmobile.data.repository

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.local.dao.ImageDao
import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.data.source.local.db.AppDatabase
import com.example.inspixmobile.data.source.remote.dto.CollectionMeta
import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.Image
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class CollectionRepository(
    private val database: AppDatabase,
    private val collectionDao: CollectionDao,
    private val imageDao: ImageDao,
    private val userDao: UserDao,
    private val client: HttpClient,
    private val json: Json,
) : ICollectionRepository {

    override fun getCollectionsPaging(
        userUuid: String?,
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
                    userUuid = userUuid,
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

    override suspend fun fetchRemoteCollections(
        limit: Int,
        offset: Int,
        topicId: Int?
    ): Response<List<CollectionResponseDto>, CollectionMeta> {
        val body = client.get("v1/collections/random") {
            parameter("limit", limit)
            parameter("offset", offset)

            topicId?.takeIf { it > 0 }?.let {
                parameter("topic_id", it)
            }
        }.bodyAsText()

        return json.decodeFromString(body)
    }

    override fun getExploreCollectionsPaging(
        collectionUuid: String,
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
                ExploreCollectionsPagingSource(
                    pageSize = pageSize,
                    fetchPage = { limit, offset ->
                        fetchExploreCollections(collectionUuid, limit, offset)
                    }
                )
            }
        ).flow
    }

    override suspend fun fetchExploreCollections(
        collectionUuid: String,
        limit: Int,
        offset: Int
    ): Response<List<CollectionResponseDto>, CollectionMeta> {

        val response = client.get("v1/collections/$collectionUuid/explore") {
            parameter("limit", limit)
            parameter("offset", offset)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Http error: ${response.status}")
        }

        val body = response.bodyAsText()

        return json.decodeFromString(body)
    }

    override fun getCollectionsByQuery(
        query: String,
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
                QueryCollectionsPagingSource(
                    pageSize = pageSize,
                    fetchPage = { limit, offset ->
                        fetchCollectionsByQuery(query = query, offset = offset, limit = limit)
                    }
                )
            }
        ).flow
    }

    override suspend fun fetchCollectionsByQuery(
        query: String,
        offset: Int,
        limit: Int
    ): Response<List<CollectionResponseDto>, CollectionMeta> {
        try {
            val response = client.get("v1/collections/search") {
                parameter("limit", limit)
                parameter("offset", offset)
                parameter("searchKey", query)
            }

            Log.i("myapp", limit.toString())

            val body = response.bodyAsText()

            return json.decodeFromString(body)
        } catch (e: Exception) {
            Log.e("myapp", "${e.message}")

            throw Exception("Failed to fetch collections by query: ${e.message}", e)
        }
    }

    override fun getCachedCollections(): Flow<List<Collection>> = flow {
        collectionDao.getListCollectionsWithImagesFlow().collect { collectionWithImages ->
            val collections = collectionWithImages.map { it.toDomain() }
            emit(collections)
        }
    }

    override fun getRecommendedCollections(): Flow<List<List<Collection>>> = flow {
        emitAll(collectionDao.getRecommendedCollections().map { list ->
            list.map { it.toDomain() }
                .groupBy { it.author?.uuid }
                .values
                .toList()
        })
    }

    override fun getOwnedCollectionsCount(uuid: String): Flow<Int> = flow {
        emitAll(collectionDao.observeOwnedCollectionsCount(uuid))
    }

    override fun getLikedCollectionsCount(uuid: String): Flow<Int> = flow {
        emitAll(collectionDao.observeLikedCollectionsCount(uuid))
    }

    override suspend fun uploadCollection(
        context: Context,
        title: String,
        description: String,
        selectedTopicId: Int,
        images: List<Image>,
        onProgress: (Float) -> Unit
    ): Response<CollectionResponseDto, Unit> {

        val response = client.submitFormWithBinaryData(
            url = "v1/collections/upload",
            formData = formData {
                append("title", title)
                append("description", description)
                append("topic_id", selectedTopicId.toString())

                images.forEach { image ->
                    val uri = image.uri ?: return@forEach
                    val parsedUri = uri.toUri()

                    val mimeType = context.contentResolver.getType(parsedUri)
                        ?: "image/jpeg"

                    val bytes = context.contentResolver.openInputStream(parsedUri)
                        ?.use { it.readBytes() }
                        ?: error("Cannot read image at $uri")

                    val extension = when (mimeType) {
                        "image/png" -> "png"
                        "image/webp" -> "webp"
                        "image/gif" -> "gif"
                        else -> "jpg"
                    }

                    append(
                        key = "images[]",
                        value = bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, mimeType)
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=\"upload_${System.currentTimeMillis()}.$extension\""
                            )
                        }
                    )
                }
            }
        ) {
            headers.remove(HttpHeaders.ContentType)
            onUpload { bytesSentTotal, contentLength ->
                if (contentLength != null && contentLength > 0) {
                    onProgress((bytesSentTotal.toFloat() / contentLength).coerceIn(0f, 1f))
                } else {
                    onProgress(-1f)
                }
            }
        }

        val body = response.bodyAsText()
        return json.decodeFromString(body)
    }
}

private class AllCollectionsPagingSource(
    private val database: AppDatabase,
    private val userUuid: String? = null,
    private val collectionDao: CollectionDao,
    private val imageDao: ImageDao,
    private val userDao: UserDao,
    private val pageSize: Int,
    private val fetchPage: suspend (limit: Int, offset: Int) -> Response<List<CollectionResponseDto>, CollectionMeta>
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

            val items = response.data.orEmpty().map { it.toDomain() }
            cacheCollections(items)

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
        collections: List<Collection>
    ) {
        val collectionEntities = collections.map { it.toEntity() }
        val imageEntities = collections.flatMap { collection ->
            collection.images.orEmpty().map { image ->
                image.copy(collectionUuid = image.collectionUuid ?: collection.uuid).toEntity()
            }
        }
        val userEntities = collections
            .map { it.author }
            .map { it?.toEntity() }
            .filter { it?.uuid != userUuid }
            .filterNotNull()

        database.withTransaction {
            userDao.resetFollowedStatus()

            userDao.upsertAll(userEntities)
            collectionDao.upsertAll(collectionEntities)
            imageDao.upsertAll(imageEntities)
        }
    }

    private suspend fun loadFromCacheOrError(
        throwable: Throwable,
        offset: Int
    ): LoadResult<Int, Collection> {
        if (offset > 0) {
            return LoadResult.Error(throwable)
        }

        val cached = collectionDao.getListCollectionsWithImagesAndAuthor()
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
    private val fetchPage: suspend (limit: Int, offset: Int) -> Response<List<CollectionResponseDto>, CollectionMeta>
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

            val items = response.data.orEmpty().map { it.toDomain() }
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

private class ExploreCollectionsPagingSource(
    private val pageSize: Int,
    private val fetchPage: suspend (limit: Int, offset: Int) -> Response<List<CollectionResponseDto>, CollectionMeta>
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

            val items = response.data.orEmpty().map { it.toDomain() }
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

private class QueryCollectionsPagingSource(
    private val pageSize: Int,
    private val fetchPage: suspend (limit: Int, offset: Int) -> Response<List<CollectionResponseDto>, CollectionMeta>
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

            val items = response.data.orEmpty().map { it.toDomain() }
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