package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.response.CollectionResponseDto
import com.example.inspixmobile.data.entity.CollectionEntity
import com.example.inspixmobile.domain.model.Collection

fun CollectionResponseDto.toDomain() = Collection(
    id = id,
    userId = user_id,
    topicId = topic_id ?: topic?.id,
    title = title,
    description = description,
    topicName = topic?.name,
    totalLikes = total_likes,
    images = images?.map { it.toDomain() },
    author = author?.toDomain(),
    createdAt = created_at_human ?: created_at
)

fun CollectionEntity.toDomain() = Collection(
    id = id,
    userId = userId,
    topicId = topicId,
    title = title,
    description = description,
    totalLikes = totalLikes
)

fun Collection.toEntity() = CollectionEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    topicId = topicId,
    totalLikes = totalLikes
)
