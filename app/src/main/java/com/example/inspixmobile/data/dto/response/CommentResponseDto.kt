package com.example.inspixmobile.data.dto.response

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CommentResponseDto(
    val id: Long? = null,
    val user_id: Long? = null,
    val collection_id: Long? = null,
    val parent_id: Long? = null,
    val content: String? = null,
    @Contextual
    val created_at: LocalDateTime? = null,
    @Contextual
    val updated_at: LocalDateTime? = null
)

