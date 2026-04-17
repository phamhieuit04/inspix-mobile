package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.response.ImageResponseDto
import com.example.inspixmobile.data.entity.ImageEntity
import com.example.inspixmobile.domain.model.Image

fun ImageResponseDto.toDomain() = Image(
    uuid = uuid,
    userId = user_id,
    collectionId = collection_id,
    urlSmall = url_small,
    urlRegular = url_regular,
    urlFull = url_full,
    downloadUrl = download_url
)

fun ImageEntity.toDomain() = Image(
    uuid = uuid,
    userId = userId,
    collectionId = collectionId,
    urlSmall = urlSmall,
    urlRegular = urlRegular,
    urlFull = urlFull,
    downloadUrl = downloadUrl
)

fun Image.toEntity() = ImageEntity(
    uuid = uuid,
    userId = userId,
    collectionId = collectionId,
    urlSmall = urlSmall,
    urlRegular = urlRegular,
    urlFull = urlFull,
    downloadUrl = downloadUrl
)