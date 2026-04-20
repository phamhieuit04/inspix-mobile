package com.example.inspixmobile.domain.model

data class Collection(
    val id: Long? = null,
    val userId: Long? = null,
    val topicId: Int? = null,

    val title: String? = null,
    val description: String? = null,
    val topicName: String? = null,
    val isLiked: Boolean? = null,
    val totalLikes: Int? = null,
    val images: List<Image>? = null,
    val author: User? = null,
    val createdAt: String? = null,
    val createdAtHuman: String? = null,
    val updatedAt: String? = null,
    val updatedAtHuman: String? = null
)
