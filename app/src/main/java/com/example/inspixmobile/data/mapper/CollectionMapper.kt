package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.local.entity.CollectionEntity
import com.example.inspixmobile.data.source.local.relationship.CollectionWithImages
import com.example.inspixmobile.domain.model.Collection

fun CollectionResponseDto.toDomain() = Collection(
    uuid = uuid,
    userUuid = user_uuid,
    topicId = topic_id ?: topic?.id,
    title = title,
    description = description,
    topicName = topic?.name,
    totalLikes = total_likes,
    images = images?.map { it.toDomain() },
    author = author?.toDomain(),
    createdAt = created_at,
    createdAtHuman = created_at_human,
    updatedAt = updated_at,
    updatedAtHuman = updated_at_human
)

fun CollectionEntity.toDomain() = Collection(
    uuid = uuid,
    userUuid = userUuid,
    topicId = topicId,
    title = title,
    description = description,
    totalLikes = totalLikes,
    createdAt = createdAt?.toString(),
    createdAtHuman = createdAtHuman,
    updatedAt = updatedAt?.toString(),
    updatedAtHuman = updatedAtHuman
)

fun CollectionWithImages.toDomain() = collection.toDomain().copy(
    images = images.map { it.toDomain() }
)

fun Collection.toEntity() = CollectionEntity(
    uuid = uuid ?: "",
    userUuid = userUuid,
    title = title,
    description = description,
    topicId = topicId,
    createdAt = null,
    createdAtHuman = createdAtHuman,
    updatedAt = null,
    updatedAtHuman = updatedAtHuman,
    totalLikes = totalLikes
)
