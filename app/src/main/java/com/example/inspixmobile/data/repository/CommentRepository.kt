package com.example.inspixmobile.data.repository

import android.util.Log
import com.example.inspixmobile.data.source.remote.dto.CommentResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.contract.repository.ICommentRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class CommentRepository(
    private val client: HttpClient,
    private val json: Json,
) : ICommentRepository {

    override suspend fun getCommentsByCollectionUuid(collectionUuid: String): Response<CommentResponseDto> {
        val body = client.get("v1/collections/$collectionUuid/comments").bodyAsText()
        val result = json.decodeFromString<Response<CommentResponseDto>>(body)
        Log.i("myapp", "$result")

        return result
    }
}