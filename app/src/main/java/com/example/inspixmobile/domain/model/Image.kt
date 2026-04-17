package com.example.inspixmobile.domain.model

data class Image(
    val uuid: String? = null,
    val userId: Long? = null,
    val collectionId: Long? = null,

    val urlSmall: String? = null,
    val urlRegular: String? = null,
    val urlFull: String? = null,
    val downloadUrl: String? = null,
    val isLiked: Boolean? = null,
    val totalLikes: Int? = null
)
