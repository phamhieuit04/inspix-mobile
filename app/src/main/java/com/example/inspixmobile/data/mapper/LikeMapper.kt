package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.response.LikeResponseDto
import com.example.inspixmobile.data.entity.LikeEntity
import com.example.inspixmobile.domain.model.Like

fun LikeResponseDto.toDomain() = Like(
    id = id,
    userId = user_id,
    collectionId = collection_id,
    createdAt = created_at,
    updatedAt = updated_at
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

