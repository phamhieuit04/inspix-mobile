package com.example.inspixmobile.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.local.dao.ImageDao
import com.example.inspixmobile.data.source.local.db.AppDatabase
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.model.Collection
import io.ktor.client.HttpClient
import io.ktor.client.request.get
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

    override fun getCollections(): Flow<List<Collection>> {
        return collectionDao.getCollectionsWithImages()
            .map { collections -> collections.map { it.toDomain() } }
    }

    override suspend fun refreshCollections() {
        try {
            val remoteCollections = fetchRemoteCollections().map { it.toDomain() }
            val collectionEntities = remoteCollections.map { it.toEntity() }
            val imageEntities = remoteCollections.flatMap { collection ->
                collection.images.orEmpty().map { image ->
                    image.copy(collectionId = image.collectionId ?: collection.id).toEntity()
                }
            }

            database.withTransaction {
                imageDao.clearAll()
                collectionDao.clearAll()
                collectionDao.insertAll(collectionEntities)
                imageDao.insertAll(imageEntities)
            }
        } catch (e: Exception) {
            Log.e("myapp", "Failed to refresh collections", e)
        }
    }

    private suspend fun fetchRemoteCollections(): List<CollectionResponseDto> {
        val body = client.get("v1/images/random").bodyAsText()
        val response = json.decodeFromString<Response<CollectionResponseDto>>(body)
        return response.data?.items.orEmpty()
    }
}