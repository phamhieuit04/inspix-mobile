package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.data.source.remote.dto.UserResponseDto
import com.example.inspixmobile.domain.model.User
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    suspend fun fetchUser(uuid: String): Response<UserResponseDto, Unit>

    suspend fun refreshUser(uuid: String)

    fun findUser(uuid: String): Flow<User?>
}