package com.example.inspixmobile.domain.model

import java.time.LocalDateTime

data class User(
    val uuid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val password: String? = null,
    val avatarUrl: String? = null,
    val totalCollections: Int? = null,
    val totalLikes: Int? = null,
    val totalImages: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
