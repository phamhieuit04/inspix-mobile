package com.example.inspixmobile.data.source.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionResponseDto(
    val items: List<CollectionResponseDto>? = null,
    val meta: CollectionMetaResponseDto? = null,

    val uuid: String? = null,
    val user_uuid: String? = null,
    val title: String? = null,
    val description: String? = null,
    val topic_id: Int? = null,
    val total_likes: Int? = null,
    val total_comments: Int? = null,
    val images: List<ImageResponseDto>? = null,
    val author: UserResponseDto? = null,
    val topic: TopicResponseDto? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val created_at_human: String? = null,
    val updated_at_human: String? = null
)

@Serializable
data class CollectionMetaResponseDto(
    val limit: Int? = null,
    val offset: Int? = null,
    val count: Int? = null,
    val total: Int? = null
)

