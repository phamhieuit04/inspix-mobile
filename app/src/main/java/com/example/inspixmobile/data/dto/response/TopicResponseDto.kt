package com.example.inspixmobile.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TopicResponseDto(
    val id: Int? = null,
    val name: String? = null
)

