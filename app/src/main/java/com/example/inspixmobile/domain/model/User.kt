package com.example.inspixmobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uuid: String? = null,
    val name: String? = null,
    val username: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val password: String? = null,
    val avatarUrl: String? = null,
    val totalCollections: Int? = null,
    val totalLikes: Int? = null,
    val totalImages: Int? = null,
    val ownedCollections: List<Collection>? = null,
    val likedCollections: List<Collection>? = null,
    val isFollowed: Boolean? = null,
    val followers: Int = 0,
    val following: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
