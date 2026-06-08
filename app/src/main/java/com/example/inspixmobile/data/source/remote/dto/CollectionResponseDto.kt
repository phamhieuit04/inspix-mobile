package com.example.inspixmobile.data.source.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionResponseDto(
    val uuid: String? = null,
    val user_uuid: String? = null,
    val title: String? = null,
    val description: String? = null,
    val topic_id: Int? = null,
    val total_likes: Int? = null,
    val total_comments: Int? = null,
    val latest_comment: CommentResponseDto? = null,
    val is_liked: Boolean = false,
    val images: List<ImageResponseDto>? = null,
    val author: UserResponseDto? = null,
    val topic: TopicResponseDto? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val created_at_human: String? = null,
    val updated_at_human: String? = null
)

@Serializable
data class CollectionMeta(
    val limit: Int? = null,
    val offset: Int? = null,
    val count: Int? = null,
    val total: Int? = null,
    val has_more: Boolean? = null
)