package com.example.inspixmobile.data.dto

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class FollowerDto(
    val id: Long? = null,
    val user_id: Long? = null,
    val author_id: Long? = null,
    @Contextual
    val created_at: LocalDateTime? = null,
    @Contextual
    val updated_at: LocalDateTime? = null
)
