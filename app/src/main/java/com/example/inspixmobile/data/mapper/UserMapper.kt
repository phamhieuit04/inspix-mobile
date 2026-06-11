package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.UserResponseDto
import com.example.inspixmobile.data.source.local.entity.UserEntity
import com.example.inspixmobile.core.util.UrlHelper
import com.example.inspixmobile.data.source.remote.dto.ProfileResponseDto
import com.example.inspixmobile.data.source.remote.dto.SignInResponseDto
import com.example.inspixmobile.domain.model.User

fun UserResponseDto.toDomain() = User(
    uuid = uuid,
    name = name,
    username = username,
    email = email,
    bio = bio,
    password = password,
    avatarUrl = UrlHelper.resolveMediaUrl(avatar_url),
    totalCollections = total_collections,
    totalLikes = total_likes,
    totalImages = total_images,
    createdAt = created_at,
    updatedAt = updated_at
)

fun UserEntity.toDomain() = User(
    uuid = uuid,
    name = name,
    username = username,
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
    uuid = uuid ?: "",
    name = name,
    username = username,
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

fun SignInResponseDto.toDomain() = User(
    uuid = user?.uuid,
    name = user?.name,
    email = user?.email,
    bio = user?.bio,
    password = user?.password,
    avatarUrl = UrlHelper.resolveMediaUrl(user?.avatar_url),
    totalCollections = user?.total_collections,
    totalLikes = user?.total_likes,
    totalImages = user?.total_images,
    createdAt = user?.created_at,
    updatedAt = user?.updated_at
)

fun ProfileResponseDto.toDomain() = User(
    uuid = user?.uuid,
    name = user?.name,
    email = user?.email,
    bio = user?.bio,
    password = user?.password,
    avatarUrl = UrlHelper.resolveMediaUrl(user?.avatar_url),
    totalCollections = user?.total_collections,
    totalLikes = user?.total_likes,
    totalImages = user?.total_images,
    ownedCollections = owned?.map { it.toDomain() },
    likedCollections = liked?.map { it.toDomain() },
    createdAt = user?.created_at,
    updatedAt = user?.updated_at
)