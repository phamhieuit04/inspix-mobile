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
    val username: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val password: String? = null,
    val avatar_url: String? = null,
    val total_collections: Int? = null,
    val total_likes: Int? = null,
    val total_images: Int? = null,
    val is_followed: Boolean? = null,
    val followers: Int = 0,
    val following: Int = 0,
    @Contextual
    val created_at: String? = null,
    @Contextual
    val updated_at: String? = null
)

@Serializable
data class ProfileResponseDto(
    val user: UserResponseDto? = null,
    val owned: List<CollectionResponseDto>? = null,
    val liked: List<CollectionResponseDto>? = null
)
