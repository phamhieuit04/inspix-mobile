package com.example.inspixmobile.data.source.remote.dto

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class FollowerResponseDto(
    val created: Boolean = false
)


