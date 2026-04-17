package com.example.inspixmobile.domain.model

data class Collection(
    val id: Long? = null,
    val userId: Long? = null,
    val topicId: Int? = null,

    val title: String? = null,
    val description: String? = null,
    val topicName: String? = null
)
