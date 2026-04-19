package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.FollowerResponseDto
import com.example.inspixmobile.data.source.local.entity.FollowerEntity
import com.example.inspixmobile.domain.model.Follower

fun FollowerResponseDto.toDomain() = Follower(
    id = id,
    userId = user_id,
    authorId = author_id,
    createdAt = created_at,
    updatedAt = updated_at
)

fun FollowerEntity.toDomain() = Follower(
    id = id,
    userId = userId,
    authorId = authorId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Follower.toEntity() = FollowerEntity(
    id = id,
    userId = userId,
    authorId = authorId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

