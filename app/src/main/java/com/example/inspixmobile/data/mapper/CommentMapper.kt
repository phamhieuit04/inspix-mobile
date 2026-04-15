package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.CommentDto
import com.example.inspixmobile.data.entity.CommentEntity
import com.example.inspixmobile.data.model.Comment

fun CommentDto.toDomain() = Comment(
    id = id,
    userId = user_id,
    collectionId = collection_id,
    parentId = parent_id,
    content = content,
    createdAt = created_at,
    updatedAt = updated_at
)

fun Comment.toDto() = CommentDto(
    id = id,
    user_id = userId,
    collection_id = collectionId,
    parent_id = parentId,
    content = content,
    created_at = createdAt,
    updated_at = updatedAt
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

