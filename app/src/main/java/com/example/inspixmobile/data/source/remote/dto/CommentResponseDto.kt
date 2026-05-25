package com.example.inspixmobile.data.source.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentResponseDto(
    val id: Long? = null,
    val collection_uuid: String? = null,
    val parent_id: Long? = null,
    val context: String? = null,
    val user: UserResponseDto? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val created_at_human: String? = null,
    val updated_at_human: String? = null
)

@Serializable
data class CommentMeta(
    val count: Int? = null
)
