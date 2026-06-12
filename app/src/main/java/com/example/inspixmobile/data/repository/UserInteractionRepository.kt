package com.example.inspixmobile.data.repository

import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.domain.contract.repository.IUserInteractionRepository
import com.example.inspixmobile.domain.model.User
import com.example.inspixmobile.presentation.state.UserInteractionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserInteractionRepository(
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

    override suspend fun toggleFollow(user: User) {
        val userUuid = user.uuid ?: return
        val current = _interactions.value[userUuid] ?: return

        val optimistic = current.copy(
            isFollowed = !current.isFollowed
        )

        _interactions.update {
            it + (userUuid to optimistic)
        }
    }

    override fun clear() {
        _interactions.value = emptyMap()
    }
}