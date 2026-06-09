package com.example.inspixmobile.data.repository

import android.util.Log
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.data.source.local.relationship.CollectionWithImages
import com.example.inspixmobile.data.source.remote.dto.ProfileResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.contract.repository.IUserRepository
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.User
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class UserRepository(
    private val client: HttpClient,
    private val json: Json,
    private val userDao: UserDao,
    private val collectionDao: CollectionDao
) : IUserRepository {

    override fun observeOwnedCollections(): Flow<List<Collection>> =
        collectionDao.observeCollectionsBySource(CollectionSource.OWNED)
            .map { list -> list.map { it.toDomain() } }

    override fun observeLikedCollections(): Flow<List<Collection>> =
        collectionDao.observeCollectionsBySource(CollectionSource.LIKED)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun fetchProfile(uuid: String): Response<ProfileResponseDto, Unit> {
        val body = client.get("v1/profile").bodyAsText()

        return json.decodeFromString(body)
    }

    override suspend fun refreshProfile(uuid: String) {
        val response = try {
            fetchProfile(uuid)
        } catch (e: Exception) {
            Log.w("myapp", "Refresh profile failed", e)
            return
        }

        if (response.success != true) {
            Log.w("myapp", "Refresh profile unsuccessful: ${response.message}")
            return
        }

        val data = response.data ?: return

        collectionDao.clearBySource(CollectionSource.OWNED)
        collectionDao.insertAll(
            data.owned?.map { it.toDomain().toEntity(source = CollectionSource.OWNED) }
                ?: emptyList()
        )

        collectionDao.clearBySource(CollectionSource.LIKED)
        collectionDao.insertAll(
            data.liked?.map { it.toDomain().toEntity(source = CollectionSource.LIKED) }
                ?: emptyList()
        )
    }

    override fun findProfile(uuid: String): Flow<User?> = flow {
        try {
            refreshProfile(uuid)
        } catch (e: Exception) {
            Log.e("myapp", "${e.message}")
        }

        emitAll(userDao.observeUser(uuid).map { it?.toDomain() })
    }
}

object CollectionSource {
    const val OWNED = "owned"
    const val LIKED = "liked"
}