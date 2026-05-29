package com.example.inspixmobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Topic(
    val id: Int? = null,
    val name: String? = null,
    val thumbnailUrl: String? = null
)
