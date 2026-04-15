package com.example.inspixmobile.data.model

import java.time.LocalDateTime

data class UserInterested(
    val id: Long? = null,
    val userId: Long? = null,
    val topicIds: List<Int>? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

