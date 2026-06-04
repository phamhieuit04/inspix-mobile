package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.domain.model.User

interface IAuthRepository {
    suspend fun signIn(email: String, password: String): User?

    suspend fun checkToken(token: String): User?
}