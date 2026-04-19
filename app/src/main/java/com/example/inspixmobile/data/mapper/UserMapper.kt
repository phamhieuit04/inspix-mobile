package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.UserResponseDto
import com.example.inspixmobile.data.source.local.entity.UserEntity
import com.example.inspixmobile.domain.model.User

fun UserResponseDto.toDomain() = User(
    id = id,
    name = name,
    email = email,
    bio = bio,
    password = password,
    avatarUrl = avatar_url,
    totalCollections = total_collections,
    totalLikes = total_likes,
    totalImages = total_images,
    createdAt = created_at,
    updatedAt = updated_at
)

fun UserEntity.toDomain() = User(
    id = id,
    name = name,
    email = email,
    bio = bio,
    password = password,
    avatarUrl = avatarUrl,
    totalCollections = totalCollections,
    totalLikes = totalLikes,
    totalImages = totalImages,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun User.toEntity() = UserEntity(
    id = id,
    name = name,
    email = email,
    bio = bio,
    password = password,
    avatarUrl = avatarUrl,
    totalCollections = totalCollections,
    totalLikes = totalLikes,
    totalImages = totalImages,
    createdAt = createdAt,
    updatedAt = updatedAt
)

