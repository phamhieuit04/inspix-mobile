package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.CommentResponseDto
import com.example.inspixmobile.data.source.local.entity.CommentEntity
import com.example.inspixmobile.data.source.local.relationship.CommentWithUser
import com.example.inspixmobile.domain.model.Comment

fun CommentResponseDto.toDomain() = Comment(
    id = id,
    userUuid = user?.uuid,
    collectionUuid = collection_uuid,
    parentId = parent_id,
    user = user?.toDomain(),
    content = context,
    createdAt = created_at_human,
    updatedAt = updated_at_human
)

fun CommentEntity.toDomain() = Comment(
    id = id,
    userUuid = userUuid,
    collectionUuid = collectionUuid,
    parentId = parentId,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Comment.toEntity() = CommentEntity(
    id = id,
    userUuid = userUuid,
    collectionUuid = collectionUuid,
    parentId = parentId,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CommentWithUser.toDomain() = Comment(
    id = comment.id,
    userUuid = comment.userUuid,
    collectionUuid = comment.collectionUuid,
    parentId = comment.parentId,

    user = user?.toDomain(),

    content = comment.content,
    createdAt = comment.createdAt,
    updatedAt = comment.updatedAt
)