package com.example.inspixmobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: Long? = null,
    val userUuid: String? = null,
    val collectionUuid: String? = null,
    val parentId: Long? = null,

    val user: User? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
