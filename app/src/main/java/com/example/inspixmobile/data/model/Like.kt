package com.example.inspixmobile.data.model

import java.time.LocalDateTime

data class Like(
    val id: Long? = null,
    val userId: Long? = null,
    val collectionId: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
