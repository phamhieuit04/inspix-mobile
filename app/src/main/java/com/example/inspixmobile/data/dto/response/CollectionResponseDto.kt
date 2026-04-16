package com.example.inspixmobile.data.dto.response

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CollectionResponseDto(
    val id: Long? = null,
    val user_id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val topic_id: Int? = null,
    val total_likes: Int? = null,
    @Contextual
    val created_at: LocalDateTime? = null,
    @Contextual
    val updated_at: LocalDateTime? = null
)

