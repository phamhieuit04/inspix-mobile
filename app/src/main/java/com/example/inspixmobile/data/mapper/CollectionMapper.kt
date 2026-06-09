package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.local.entity.CollectionEntity
import com.example.inspixmobile.data.source.local.relationship.CollectionWithImages
import com.example.inspixmobile.data.source.local.relationship.CollectionWithImagesAndAuthor
import com.example.inspixmobile.domain.model.Collection

fun CollectionResponseDto.toDomain() = Collection(
    uuid = uuid,
    userUuid = user_uuid ?: author?.uuid,
    topicId = topic_id ?: topic?.id,
    title = title,
    description = description,
    topicName = topic?.name,
    totalLikes = total_likes,
    totalComments = total_comments,
    lastestComment = latest_comment?.toDomain(),
    isLiked = is_liked,
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
    topicName = topicName,
    title = title,
    description = description,
    totalLikes = totalLikes,
    totalComments = totalComments,
    isLiked = isLiked,
    createdAt = createdAt?.toString(),
    createdAtHuman = createdAtHuman,
    updatedAt = updatedAt?.toString(),
    updatedAtHuman = updatedAtHuman
)

fun CollectionWithImagesAndAuthor.toDomain() = collection.toDomain().copy(
    images = images.map { it.toDomain() },
    author = author?.toDomain()
)

fun CollectionWithImages.toDomain() = collection.toDomain().copy(
    images = images.map { it.toDomain() }
)

fun Collection.toEntity() = CollectionEntity(
    uuid = uuid ?: "",
    userUuid = author?.uuid ?: userUuid,
    title = title,
    description = description,
    topicId = topicId,
    topicName = topicName,
    createdAt = null,
    createdAtHuman = createdAtHuman,
    updatedAt = null,
    updatedAtHuman = updatedAtHuman,
    totalLikes = totalLikes,
    totalComments = totalComments,
    isLiked = isLiked == true
)