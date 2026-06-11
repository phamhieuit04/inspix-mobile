package com.example.inspixmobile.domain.contract.repository

import androidx.paging.PagingData
import com.example.inspixmobile.data.source.remote.dto.CollectionMeta
import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.data.source.remote.dto.UserResponseDto
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.User
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun observeOwnedCollections(uuid: String): Flow<List<Collection>>

    suspend fun fetchProfile(uuid: String): Response<UserResponseDto, Unit>

    suspend fun fetchLikedCollections(
        offset: Int,
        limit: Int
    ): Response<List<CollectionResponseDto>, CollectionMeta>

    suspend fun refreshProfile(uuid: String, offset: Int, limit: Int)

    fun findProfile(uuid: String): Flow<User?>

    fun getLikedCollectionsPager(
        userUuid: String,
        pageSize: Int,
        prefetchDistance: Int
    ): Flow<PagingData<Collection>>
}