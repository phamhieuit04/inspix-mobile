package com.example.inspixmobile.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.data.source.local.db.AppDatabase
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
    private val database: AppDatabase,
    private val userDao: UserDao,
    private val collectionDao: CollectionDao
) : IUserRepository {

    override fun observeOwnedCollections(uuid: String): Flow<List<Collection>> =
        collectionDao.getOwnedCollections(uuid)
            .map { list -> list.map { it.toDomain() } }

    override fun observeLikedCollections(uuid: String): Flow<List<Collection>> =
        collectionDao.getLikedCollections(uuid)
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

        database.withTransaction {
            data.liked?.forEach { collection ->
                collectionDao.upsertLikedCollection(collection.uuid!!)
            }

            data.owned?.forEach { collection ->
                collectionDao.upsertOwnedCollection(collection.toDomain().toEntity())
            }
        }
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