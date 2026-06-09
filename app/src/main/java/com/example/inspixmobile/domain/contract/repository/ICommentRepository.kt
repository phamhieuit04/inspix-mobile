package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.data.source.remote.dto.CommentMeta
import com.example.inspixmobile.data.source.remote.dto.CommentResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface ICommentRepository {
    fun getCommentsByCollectionUuid(uuid: String): Flow<List<Comment>>

    suspend fun fetchCommentsByCollectionUuid(uuid: String): Response<List<CommentResponseDto>, CommentMeta>

    suspend fun refreshComments(collectionUuid: String)
}