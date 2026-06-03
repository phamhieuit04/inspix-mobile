package com.example.inspixmobile.data.repository

import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.CommentDao
import com.example.inspixmobile.data.source.remote.dto.CommentMeta
import com.example.inspixmobile.data.source.remote.dto.CommentResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.contract.repository.ICommentRepository
import com.example.inspixmobile.domain.model.Comment
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class CommentRepository(
    private val client: HttpClient,
    private val json: Json,
    private val commentDao: CommentDao
) : ICommentRepository {

    override fun getCommentsByCollectionUuid(collectionUuid: String): Flow<List<Comment>> = flow {
        emitAll(
            commentDao.getCommentsByCollectionUuid(collectionUuid)
                .map { it.map { entity -> entity.toDomain() } }
        )
    }

    override suspend fun refreshComments(collectionUuid: String) {
        val response = fetchCommentsByCollectionUuid(collectionUuid)

        if (response.success == true) {
            commentDao.replaceComments(
                collectionUuid,
                response.data?.map {
                    it.toDomain().toEntity()
                } ?: emptyList()
            )
        }
    }

    override suspend fun fetchCommentsByCollectionUuid(collectionUuid: String): Response<List<CommentResponseDto>, CommentMeta> {
        val body = client.get("v1/collections/$collectionUuid/comments").bodyAsText()

        return json.decodeFromString(body)
    }
}