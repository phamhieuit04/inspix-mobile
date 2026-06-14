package com.example.inspixmobile.data.repository

import android.util.Log
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.data.source.remote.dto.FollowerResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.contract.repository.IUserInteractionRepository
import com.example.inspixmobile.domain.model.User
import com.example.inspixmobile.presentation.state.InteractionState
import com.example.inspixmobile.presentation.state.UserInteractionState
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

class UserInteractionRepository(
    private val client: HttpClient,
    private val json: Json,
    private val userDao: UserDao
) : IUserInteractionRepository {

    private val _interactions =
        MutableStateFlow<Map<String, UserInteractionState>>(emptyMap())

    override val interactions = _interactions.asStateFlow()

    override fun seed(user: User) {
        val uuid = user.uuid ?: return
        if (_interactions.value.containsKey(uuid)) return

        _interactions.update {
            it + (uuid to UserInteractionState(
                isFollowed = user.isFollowed ?: false
            ))
        }
    }

    override suspend fun toggleFollow(
        user: User
    ): InteractionState {
        val userUuid = user.uuid
            ?: return InteractionState.Unknown("User UUID is null")

        val current = _interactions.value[userUuid]
            ?: return InteractionState.Unknown("Interaction state not found")

        val optimistic = current.copy(
            isFollowed = !current.isFollowed
        )

        _interactions.update {
            it + (userUuid to optimistic)
        }

        try {
            val response = client.get("v1/follow/$userUuid")

            if (response.status == HttpStatusCode.Unauthorized) {
                _interactions.update {
                    it + (userUuid to current)
                }

                return InteractionState.Unauthorized
            }

            val result = json.decodeFromString<Response<FollowerResponseDto, Unit>>(
                response.bodyAsText()
            )

            if (result.success == true) {
                val updatedUser = user.copy(
                    isFollowed = optimistic.isFollowed
                )

                userDao.upsert(updatedUser.toEntity())

                return InteractionState.Success
            }

            _interactions.update {
                it + (userUuid to current)
            }

            return InteractionState.Unknown(result.message)

        } catch (_: IOException) {
            _interactions.update {
                it + (userUuid to current)
            }

            return InteractionState.Network

        } catch (e: Exception) {
            _interactions.update {
                it + (userUuid to current)
            }

            Log.e("myapp", "toggle follow failed", e)

            return InteractionState.Unknown(e.message)
        }
    }

    override fun clear() {
        _interactions.value = emptyMap()
    }
}