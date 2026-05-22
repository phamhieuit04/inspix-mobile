package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.ImageResponseDto
import com.example.inspixmobile.data.source.local.entity.ImageEntity
import com.example.inspixmobile.core.util.UrlHelper
import com.example.inspixmobile.domain.model.Image

fun ImageResponseDto.toDomain() = Image(
    uuid = uuid,
    userUuid = user_uuid,
    collectionUuid = collection_uuid,
    color = color,
    width = width,
    height = height,
    urlSmall = UrlHelper.resolveMediaUrl(url_small),
    urlRegular = UrlHelper.resolveMediaUrl(url_regular),
    urlFull = UrlHelper.resolveMediaUrl(url_full),
    downloadUrl = UrlHelper.resolveMediaUrl(download_url),
    createdAt = created_at,
    createdAtHuman = created_at_human,
    updatedAt = updated_at,
    updatedAtHuman = updated_at_human
)

fun ImageEntity.toDomain() = Image(
    uuid = uuid,
    userUuid = userUuid,
    collectionUuid = collectionUuid,
    color = color,
    width = width,
    height = height,
    urlSmall = urlSmall,
    urlRegular = urlRegular,
    urlFull = urlFull,
    downloadUrl = downloadUrl,
    createdAt = createdAt,
    createdAtHuman = createdAtHuman,
    updatedAt = updatedAt,
    updatedAtHuman = updatedAtHuman
)

fun Image.toEntity() = ImageEntity(
    uuid = uuid ?: "",
    userUuid = userUuid,
    collectionUuid = collectionUuid,
    color = color,
    width = width,
    height = height,
    urlSmall = urlSmall,
    urlRegular = urlRegular,
    urlFull = urlFull,
    downloadUrl = downloadUrl,
    createdAt = createdAt,
    createdAtHuman = createdAtHuman,
    updatedAt = updatedAt,
    updatedAtHuman = updatedAtHuman
)