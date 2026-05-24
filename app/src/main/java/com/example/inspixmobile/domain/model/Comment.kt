package com.example.inspixmobile.domain.model

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
