package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.response.CollectionResponseDto
import com.example.inspixmobile.data.entity.CollectionEntity
import com.example.inspixmobile.domain.model.Collection

fun CollectionResponseDto.toDomain() = Collection(
    id = id,
    userId = user_id,
    topicId = topic_id,
    title = title,
    description = description,
    totalLikes = total_likes
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
