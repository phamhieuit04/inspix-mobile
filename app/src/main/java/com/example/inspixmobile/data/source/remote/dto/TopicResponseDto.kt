package com.example.inspixmobile.data.source.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TopicResponseDto(
    val id: Int? = null,
    val name: String? = null,
    val thumbnail_url: String? = null
)


