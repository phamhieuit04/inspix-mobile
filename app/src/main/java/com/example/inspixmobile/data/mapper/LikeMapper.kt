package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.LikeDto
import com.example.inspixmobile.data.entity.LikeEntity
import com.example.inspixmobile.data.model.Like

fun LikeDto.toDomain() = Like(
    id = id,
    userId = user_id,
    collectionId = collection_id,
    createdAt = created_at,
    updatedAt = updated_at
)

fun Like.toDto() = LikeDto(
    id = id,
    user_id = userId,
    collection_id = collectionId,
    created_at = createdAt,
    updated_at = updatedAt
)

fun LikeEntity.toDomain() = Like(
    id = id,
    userId = userId,
    collectionId = collectionId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Like.toEntity() = LikeEntity(
    id = id,
    userId = userId,
    collectionId = collectionId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

