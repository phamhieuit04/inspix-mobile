package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.domain.model.User
import com.example.inspixmobile.presentation.state.UserInteractionState
import kotlinx.coroutines.flow.StateFlow

interface IUserInteractionRepository {
    val interactions: StateFlow<Map<String, UserInteractionState>>

    fun seed(user: User)

    suspend fun toggleFollow(user: User)

    fun clear()
}