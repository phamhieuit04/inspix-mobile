package com.example.inspixmobile.data.model

import java.time.LocalDateTime

data class Image(
    val uuid: String? = null,
    val color: String? = null,
    val urlSmall: String? = null,
    val urlRegular: String? = null,
    val urlFull: String? = null,
    val userId: Long? = null,
    val collectionId: Long? = null,
    val downloadUrl: String? = null,
    val totalViews: Int? = null,
    val totalLikes: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
