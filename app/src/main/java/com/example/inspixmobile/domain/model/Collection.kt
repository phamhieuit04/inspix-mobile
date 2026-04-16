package com.example.inspixmobile.domain.model

data class Collection(
    val id: Long? = null,
    val userId: Long? = null,
    val topicId: Int? = null,

    val title: String? = null,
    val description: String? = null,
    val totalLikes: Int? = null,
    val isLiked: Boolean? = null,
    val images: List<Image>? = null,
    val topicName: String? = null
)
