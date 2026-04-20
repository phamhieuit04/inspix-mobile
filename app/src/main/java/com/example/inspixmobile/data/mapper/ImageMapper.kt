package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.ImageResponseDto
import com.example.inspixmobile.data.source.local.entity.ImageEntity
import com.example.inspixmobile.domain.model.Image

fun ImageResponseDto.toDomain() = Image(
    uuid = uuid,
    userUuid = user_uuid,
    collectionUuid = collection_uuid,
    urlSmall = url_small ?: urls?.small,
    urlRegular = url_regular ?: urls?.regular,
    urlFull = url_full ?: urls?.full,
    downloadUrl = download_url ?: urls?.download,
    createdAt = created_at,
    createdAtHuman = created_at_human,
    updatedAt = updated_at,
    updatedAtHuman = updated_at_human
)

fun ImageEntity.toDomain() = Image(
    uuid = uuid,
    userUuid = userUuid,
    collectionUuid = collectionUuid,
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
    urlSmall = urlSmall,
    urlRegular = urlRegular,
    urlFull = urlFull,
    downloadUrl = downloadUrl,
    createdAt = createdAt,
    createdAtHuman = createdAtHuman,
    updatedAt = updatedAt,
    updatedAtHuman = updatedAtHuman
)