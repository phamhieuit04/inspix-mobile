package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.FollowerDto
import com.example.inspixmobile.data.entity.FollowerEntity
import com.example.inspixmobile.data.model.Follower

fun FollowerDto.toDomain() = Follower(
    id = id,
    userId = user_id,
    authorId = author_id,
    createdAt = created_at,
    updatedAt = updated_at
)

fun Follower.toDto() = FollowerDto(
    id = id,
    user_id = userId,
    author_id = authorId,
    created_at = createdAt,
    updated_at = updatedAt
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

