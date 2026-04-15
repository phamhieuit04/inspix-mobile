package com.example.inspixmobile.data.model

import java.time.LocalDateTime

data class Follower(
    val id: Long? = null,
    val userId: Long? = null,
    val authorId: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
