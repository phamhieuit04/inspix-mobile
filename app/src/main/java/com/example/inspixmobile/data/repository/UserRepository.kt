package com.example.inspixmobile.data.repository

import android.util.Log
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.data.source.remote.dto.UserResponseDto
import com.example.inspixmobile.domain.contract.repository.IUserRepository
import com.example.inspixmobile.domain.model.User
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json

class UserRepository(
    private val client: HttpClient,
    private val json: Json,
    private val userDao: UserDao
) : IUserRepository {

    override suspend fun fetchUser(uuid: String): Response<UserResponseDto, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshUser(uuid: String) {
        //
    }

    override fun findUser(uuid: String): Flow<User?> = flow {
        val domainUser = userDao.observeUser(uuid).map { it?.toDomain() }
        if (domainUser != null) emit(null)

        try {
            refreshUser(uuid)
        } catch (e: Exception) {
            Log.e("myapp", "${e.message}")
        }

        emitAll(userDao.observeUser(uuid).map { it?.toDomain() })
    }
}