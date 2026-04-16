package com.example.inspixmobile.data.dto

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class UserInterestedDto(
    val id: Long? = null,
    val user_id: Long? = null,
    val topic_ids: String? = null,
    @Contextual
    val created_at: LocalDateTime? = null,
    @Contextual
    val updated_at: LocalDateTime? = null
)

