package com.example.inspixmobile.data.repository

import com.example.inspixmobile.data.source.remote.dto.CommentMeta
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

    override suspend fun fetchCommentsByCollectionUuid(collectionUuid: String): Response<List<CommentResponseDto>, CommentMeta> {
        val body = client.get("v1/collections/$collectionUuid/comments").bodyAsText()

        return json.decodeFromString(body)
    }
}