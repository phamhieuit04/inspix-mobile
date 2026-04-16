package com.example.inspixmobile.data.dto.response

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class FollowerResponseDto(
    val id: Long? = null,
    val user_id: Long? = null,
    val author_id: Long? = null,
    @Contextual
    val created_at: LocalDateTime? = null,
    @Contextual
    val updated_at: LocalDateTime? = null
)

