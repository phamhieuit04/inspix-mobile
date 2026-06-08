package com.example.inspixmobile.core.di

import com.example.inspixmobile.data.source.local.store.SessionStore
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import com.example.inspixmobile.data.source.remote.config.API_BASE_URL
import io.ktor.client.request.bearerAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> {
        val url = API_BASE_URL
        val sessionStore = get<SessionStore>()

        HttpClient() {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
            defaultRequest {
                url(url)
                headers.append(HttpHeaders.ContentType, "application/json; charset=UTF-8")

                val session = runBlocking { sessionStore.session.first() }
                if (session.isLoggedIn) {
                    bearerAuth(session.accessToken!!)
                }
            }
        }
    }
}