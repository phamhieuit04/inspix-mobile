package com.example.inspixmobile.data.source.remote.dto

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CommentResponseDto(
    val items: List<CommentResponseDto>? = null,

    val id: Long? = null,
    val collection_uuid: String? = null,
    val parent_id: Long? = null,
    val content: String? = null,
    val user: UserResponseDto? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val created_at_human: String? = null,
    val updated_at_human: String? = null
)


