package com.example.inspixmobile.data.source.remote.dto

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class LikeResponseDto(
    val created: Boolean? = null,
    val total_likes: Int? = null,
)


