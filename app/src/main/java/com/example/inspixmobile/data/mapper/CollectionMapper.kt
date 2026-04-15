package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.CollectionDto
import com.example.inspixmobile.data.entity.CollectionEntity
import com.example.inspixmobile.data.model.Collection

fun CollectionDto.toDomain() = Collection(
    id = id,
    userId = user_id,
    title = title,
    description = description,
    topicId = topic_id,
    createdAt = created_at,
    updatedAt = updated_at
)

fun Collection.toDto() = CollectionDto(
    id = id,
    user_id = userId,
    title = title,
    description = description,
    topic_id = topicId,
    created_at = createdAt,
    updated_at = updatedAt
)

fun CollectionEntity.toDomain() = Collection(
    id = id,
    userId = userId,
    title = title,
    description = description,
    topicId = topicId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Collection.toEntity() = CollectionEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    topicId = topicId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

