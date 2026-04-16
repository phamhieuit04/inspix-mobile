package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.response.CommentResponseDto
import com.example.inspixmobile.data.entity.CommentEntity
import com.example.inspixmobile.domain.model.Comment

fun CommentResponseDto.toDomain() = Comment(
    id = id,
    userId = user_id,
    collectionId = collection_id,
    parentId = parent_id,
    content = content,
    createdAt = created_at,
    updatedAt = updated_at
)

fun CommentEntity.toDomain() = Comment(
    id = id,
    userId = userId,
    collectionId = collectionId,
    parentId = parentId,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Comment.toEntity() = CommentEntity(
    id = id,
    userId = userId,
    collectionId = collectionId,
    parentId = parentId,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

