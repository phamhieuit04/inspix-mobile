package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.data.source.remote.dto.ProfileResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.User
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun observeOwnedCollections(): Flow<List<Collection>>

    fun observeLikedCollections(): Flow<List<Collection>>

    suspend fun fetchProfile(uuid: String): Response<ProfileResponseDto, Unit>

    suspend fun refreshProfile(uuid: String)

    fun findProfile(uuid: String): Flow<User?>
}