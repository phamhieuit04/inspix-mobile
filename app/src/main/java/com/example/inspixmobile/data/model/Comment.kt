package com.example.inspixmobile.data.model

import java.time.LocalDateTime

data class Comment(
    val id: Long? = null,
    val userId: Long? = null,
    val collectionId: Long? = null,
    val parentId: Long? = null,
    val content: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
