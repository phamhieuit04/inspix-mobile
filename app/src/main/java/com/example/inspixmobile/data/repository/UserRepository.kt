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
import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.data.source.local.db.AppDatabase
import com.example.inspixmobile.data.source.local.relationship.CollectionWithImagesAndAuthor
import com.example.inspixmobile.data.source.remote.dto.CollectionMeta
import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.data.source.remote.dto.UserResponseDto
import com.example.inspixmobile.domain.contract.repository.IUserRepository
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.User
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class UserRepository(
    private val client: HttpClient,
    private val json: Json,
    private val database: AppDatabase,
    private val userDao: UserDao,
    private val collectionDao: CollectionDao
) : IUserRepository {

    override suspend fun fetchProfile(uuid: String): Response<UserResponseDto, Unit> {
        val body = client.get("v1/profile/$uuid").bodyAsText()

        return json.decodeFromString(body)
    }

    override suspend fun fetchLikedCollections(
        offset: Int,
        limit: Int
    ): Response<List<CollectionResponseDto>, CollectionMeta> {
        val body = client.get("v1/collections/liked") {
            parameter("limit", limit)
            parameter("offset", offset)
        }
            .bodyAsText()

        return json.decodeFromString(body)
    }

    override suspend fun refreshProfile(uuid: String, offset: Int, limit: Int) {
        try {
            val profileDto = fetchProfile(uuid)
            if (profileDto.success != true) {
                Log.w("myapp", "Refresh profile unsuccessful: ${profileDto.message}")
                return
            }

            val domainUser = profileDto.data?.toDomain()
            userDao.upsert(domainUser?.toEntity() ?: return)
        } catch (e: Exception) {
            Log.w("myapp", "Refresh profile failed", e)
            return
        }
    }

    override fun findProfile(uuid: String): Flow<User?> = flow {
        emitAll(userDao.observeUser(uuid).map { it?.toDomain() })
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getLikedCollectionsPager(
        userUuid: String,
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = prefetchDistance,
                enablePlaceholders = false
            ),
            remoteMediator = LikedCollectionsRemoteMediator(
                userUuid = userUuid,
                database = database,
                collectionDao = collectionDao,
                fetchLikedCollections = { offset, limit -> fetchLikedCollections(offset, limit) }
            ),
            pagingSourceFactory = { collectionDao.getLikedCollectionsPagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun fetchCollectionByUser(
        user: String,
        limit: Int,
        offset: Int
    ): Response<List<CollectionResponseDto>, CollectionMeta> {
        val body = client.get("v1/user/$user/collections") {
            parameter("limit", limit)
            parameter("offset", offset)
        }
            .bodyAsText()

        return json.decodeFromString(body)
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getOwnedCollections(
        userUuid: String,
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = prefetchDistance,
                enablePlaceholders = false
            ),
            remoteMediator = OwnedCollectionsRemoteMediator(
                userUuid = userUuid,
                database = database,
                collectionDao = collectionDao,
                fetchOwnedCollections = { userUuid, offset, limit ->
                    fetchCollectionByUser(
                        userUuid,
                        offset,
                        limit
                    )
                }
            ),
            pagingSourceFactory = { collectionDao.getOwnedCollectionsPagingSource(userUuid) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
}

@OptIn(ExperimentalPagingApi::class)
private class LikedCollectionsRemoteMediator(
    private val userUuid: String,
    private val database: AppDatabase,
    private val collectionDao: CollectionDao,
    private val fetchLikedCollections: suspend (offset: Int, limit: Int) -> Response<List<CollectionResponseDto>, CollectionMeta>
) : RemoteMediator<Int, CollectionWithImagesAndAuthor>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CollectionWithImagesAndAuthor>
    ): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> 0

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> collectionDao.countLikedCollections(userUuid)
        }

        try {
            val response = fetchLikedCollections(offset, state.config.pageSize)

            if (response.success != true) {
                return MediatorResult.Error(Exception(response.message ?: "Unknown error"))
            }

            val collections = response.data ?: emptyList()
            val hasMore = response.meta?.has_more == true

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    collectionDao.clearLikedCollections(userUuid)
                }
                val entities = collections.map { dto ->
                    dto.toDomain().toEntity().copy(isLiked = true)
                }
                collectionDao.insertAll(entities)
            }

            return MediatorResult.Success(endOfPaginationReached = !hasMore)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}

@OptIn(ExperimentalPagingApi::class)
private class OwnedCollectionsRemoteMediator(
    private val userUuid: String,
    private val database: AppDatabase,
    private val collectionDao: CollectionDao,
    private val fetchOwnedCollections: suspend (userUuid: String, offset: Int, limit: Int) -> Response<List<CollectionResponseDto>, CollectionMeta>
) : RemoteMediator<Int, CollectionWithImagesAndAuthor>() {

    override suspend fun initialize(): InitializeAction {
        val count = collectionDao.countCollectionsByUser(userUuid)
        return if (count > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CollectionWithImagesAndAuthor>
    ): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> 0

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> collectionDao.countCollectionsByUser(userUuid)
        }

        try {
            val response = fetchOwnedCollections(userUuid, offset, state.config.pageSize)

            if (response.success != true) {
                return MediatorResult.Error(Exception(response.message ?: "Unknown error"))
            }

            val collections = response.data ?: emptyList()
            val hasMore = response.meta?.has_more == true

            val domainCollections = collections.map { dto ->
                dto.toDomain()
            }
            val collectionsEntity = domainCollections.map { it.toEntity() }

            database.withTransaction {
                collectionDao.upsertAll(collectionsEntity)
            }

            return MediatorResult.Success(endOfPaginationReached = !hasMore)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}