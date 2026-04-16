package com.example.inspixmobile.domain.model

import java.time.LocalDateTime

data class Collection(
    val id: Long? = null,
    val userId: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val topicId: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
