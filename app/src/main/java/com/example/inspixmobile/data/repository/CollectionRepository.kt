package com.example.inspixmobile.data.repository

import android.util.Log
import com.example.inspixmobile.data.dto.response.CollectionResponseDto
import com.example.inspixmobile.data.dto.response.Response
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class CollectionRepository(
    private val client: HttpClient,
    private val json: Json
) : ICollectionRepository {
    override suspend fun getCollections(): List<CollectionResponseDto>? {
        return try {
            val body = client.get("v1/images/random").bodyAsText()
            val response = json.decodeFromString<Response<CollectionResponseDto>>(body)
            val data = response.data?.items.orEmpty()

            Log.i("myapp", "Fetched collections: $data")
            data
        } catch (e: Exception) {
            Log.e("myapp", "Failed to decode collections response", e)
            emptyList()
        }
    }
}