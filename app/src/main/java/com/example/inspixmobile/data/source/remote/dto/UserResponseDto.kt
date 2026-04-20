package com.example.inspixmobile.data.source.remote.dto

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    @Serializable(with = FlexibleLongSerializer::class)
    val id: Long? = null,
    val name: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val password: String? = null,
    val avatar_url: String? = null,
    val total_collections: Int? = null,
    val total_likes: Int? = null,
    val total_images: Int? = null,
    @Contextual
    val created_at: LocalDateTime? = null,
    @Contextual
    val updated_at: LocalDateTime? = null
)


