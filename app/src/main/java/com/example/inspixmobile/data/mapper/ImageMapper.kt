package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.ImageDto
import com.example.inspixmobile.data.entity.ImageEntity
import com.example.inspixmobile.domain.model.Image

fun ImageDto.toDomain() = Image(
    uuid = uuid,
    color = color,
    urlSmall = url_small,
    urlRegular = url_regular,
    urlFull = url_full,
    userId = user_id,
    collectionId = collection_id,
    downloadUrl = download_url,
    totalViews = total_views,
    totalLikes = total_likes,
    createdAt = created_at,
    updatedAt = updated_at
)

fun Image.toDto() = ImageDto(
    uuid = uuid,
    color = color,
    url_small = urlSmall,
    url_regular = urlRegular,
    url_full = urlFull,
    user_id = userId,
    collection_id = collectionId,
    download_url = downloadUrl,
    total_views = totalViews,
    total_likes = totalLikes,
    created_at = createdAt,
    updated_at = updatedAt
)

fun ImageEntity.toDomain() = Image(
    uuid = uuid,
    color = color,
    urlSmall = urlSmall,
    urlRegular = urlRegular,
    urlFull = urlFull,
    userId = userId,
    collectionId = collectionId,
    downloadUrl = downloadUrl,
    totalViews = totalViews,
    totalLikes = totalLikes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Image.toEntity() = ImageEntity(
    uuid = uuid,
    color = color,
    urlSmall = urlSmall,
    urlRegular = urlRegular,
    urlFull = urlFull,
    userId = userId,
    collectionId = collectionId,
    downloadUrl = downloadUrl,
    totalViews = totalViews,
    totalLikes = totalLikes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

