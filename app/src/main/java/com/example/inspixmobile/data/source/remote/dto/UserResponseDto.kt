package com.example.inspixmobile.data.source.remote.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class SignInResponseDto(
    val token: String? = null,
    val user: UserResponseDto? = null
)

@Serializable
data class UserResponseDto(
    val uuid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val password: String? = null,
    val avatar_url: String? = null,
    val total_collections: Int? = null,
    val total_likes: Int? = null,
    val total_images: Int? = null,
    @Contextual
    val created_at: String? = null,
    @Contextual
    val updated_at: String? = null
)


