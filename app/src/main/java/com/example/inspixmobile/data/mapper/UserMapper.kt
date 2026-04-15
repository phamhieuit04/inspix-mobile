package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.UserDto
import com.example.inspixmobile.data.entity.UserEntity
import com.example.inspixmobile.data.model.User

fun UserDto.toDomain() = User(
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

fun User.toDto() = UserDto(
    id = id,
    name = name,
    email = email,
    bio = bio,
    password = password,
    avatar_url = avatarUrl,
    total_collections = totalCollections,
    total_likes = totalLikes,
    total_images = totalImages,
    created_at = createdAt,
    updated_at = updatedAt
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

