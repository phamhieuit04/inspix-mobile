package com.example.inspixmobile.data.repository

import android.util.Log
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.data.source.local.store.SessionStore
import com.example.inspixmobile.data.source.local.store.SettingStore
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.data.source.remote.dto.SignInResponseDto
import com.example.inspixmobile.domain.contract.repository.IAuthRepository
import com.example.inspixmobile.domain.model.User
import com.example.inspixmobile.presentation.screen.HomeLayoutStyle
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class AuthRepository(
    private val client: HttpClient,
    private val json: Json,
    private val userDao: UserDao,
    private val sessionStore: SessionStore,
    private val settingStore: SettingStore
) : IAuthRepository {

    override suspend fun signIn(
        email: String,
        password: String
    ): User? {

        val response = client.submitForm(
            url = "v1/auth/sign-in",
            formParameters = parameters {
                append("email", email)
                append("password", password)
            }
        ).bodyAsText()

        val result =
            json.decodeFromString<Response<SignInResponseDto, Unit>>(response).data ?: return null

        val user = result.user?.toDomain() ?: return null
        userDao.upsert(user.toEntity())

        sessionStore.saveSession(
            accessToken = result.token.orEmpty(),
            userUuid = user.uuid.orEmpty()
        )

        EventBus.emit(Event.SignIn)

        return user
    }

    override suspend fun checkToken(token: String): User? {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        try {
            val session = sessionStore.session.first()
            val setting = settingStore.setting.first()

            val response = client.get("v1/auth/logout") {
                bearerAuth("${session.accessToken}")
            }.bodyAsText()

            val result = json.decodeFromString<Response<Unit, Unit>>(response)
            if (result.success == false) return

            sessionStore.clearSession()
            settingStore.saveSetting(
                homeLayout = HomeLayoutStyle.Grid,
                navbarLayout = setting.navbarLayout
            )

            EventBus.emit(Event.SignOut)
        } catch (e: Exception) {
            Log.e("myapp", "Logout failed: ${e.message}")
        }
    }
}