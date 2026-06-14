package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.data.source.remote.dto.CommentResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.presentation.state.CollectionInteractionState
import com.example.inspixmobile.presentation.state.InteractionState
import kotlinx.coroutines.flow.StateFlow

interface ICollectionInteractionRepository {
    val interactions: StateFlow<Map<String, CollectionInteractionState>>

    fun seed(collection: Collection)

    suspend fun toggleLike(collection: Collection): InteractionState

    suspend fun postComment(
        collectionUuid: String,
        context: String,
        parentId: Long?
    ): Response<CommentResponseDto, Unit>

    fun clear()
}