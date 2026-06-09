package com.example.inspixmobile.data.repository

import android.util.Log
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.remote.dto.LikeResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.presentation.state.CollectionInteractionState
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

class CollectionInteractionRepository(
    private val client: HttpClient,
    private val json: Json,
    private val collectionDao: CollectionDao
) : ICollectionInteractionRepository {

    private val _interactions =
        MutableStateFlow<Map<String, CollectionInteractionState>>(emptyMap())

    override val interactions = _interactions.asStateFlow()

    override fun seed(collection: Collection) {
        val uuid = collection.uuid ?: return
        if (_interactions.value.containsKey(uuid)) return

        _interactions.update {
            it + (uuid to CollectionInteractionState(
                isLiked = collection.isLiked ?: false,
                totalLikes = collection.totalLikes ?: 0,
                totalComments = collection.totalComments ?: 0
            ))
        }
    }

    override suspend fun toggleLike(collectionUuid: String) {
        val current = _interactions.value[collectionUuid] ?: return

        val optimistic = current.copy(
            isLiked = !current.isLiked,
            totalLikes =
                if (!current.isLiked) current.totalLikes + 1
                else (current.totalLikes - 1).coerceAtLeast(0)
        )

        _interactions.update {
            it + (collectionUuid to optimistic)
        }

        try {
            val response = client.post("v1/collections/$collectionUuid/like")

            if (response.status == HttpStatusCode.Unauthorized) {
                _interactions.update {
                    it + (collectionUuid to current)
                }

                EventBus.emit(Event.RequireSignIn)
                return
            }

            val result = json.decodeFromString<Response<LikeResponseDto, Unit>>(
                response.bodyAsText()
            )

            if (result.success == true) {
                collectionDao.toggleLike(
                    collectionUuid,
                    result.data?.created == true
                )

            } else {
                _interactions.update {
                    it + (collectionUuid to current)
                }
            }

        } catch (e: Exception) {
            _interactions.update {
                it + (collectionUuid to current)
            }

            Log.e("myapp", "toggleLike failed", e)
        }
    }

    override fun clear() {
        _interactions.value = emptyMap()
    }
}