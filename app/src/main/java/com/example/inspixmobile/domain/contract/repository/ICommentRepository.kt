package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.data.source.remote.dto.CommentResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response

interface ICommentRepository {
    suspend fun getCommentsByCollectionUuid(collectionUuid: String): Response<CommentResponseDto>
}