package com.example.inspixmobile.data.repository

import android.util.Log
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.data.source.remote.dto.SignInResponseDto
import com.example.inspixmobile.domain.contract.repository.IAuthRepository
import com.example.inspixmobile.domain.model.User
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import kotlinx.serialization.json.Json

class AuthRepository(
    private val client: HttpClient,
    private val json: Json
) : IAuthRepository {

    override suspend fun signIn(email: String, password: String): User? {
        val response = client.submitForm(
            url = "v1/auth/sign-in",
            formParameters = parameters {
                append(name = "email", value = email)
                append(name = "password", value = password)
            }
        ).bodyAsText()

        val result = json.decodeFromString<Response<SignInResponseDto, Unit>>(response).data

        return result?.toDomain()
    }

    override suspend fun checkToken(token: String): User? {
        TODO("Not yet implemented")
    }
}