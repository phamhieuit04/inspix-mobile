package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.core.util.UrlHelper
import com.example.inspixmobile.data.source.remote.dto.TopicResponseDto
import com.example.inspixmobile.data.source.local.entity.TopicEntity
import com.example.inspixmobile.domain.model.Topic

fun TopicResponseDto.toDomain() = Topic(
    id = id,
    name = name,
    thumbnailUrl = UrlHelper.resolveMediaUrl(thumbnail_url)
)

fun TopicEntity.toDomain() = Topic(
    id = id,
    name = name,
    thumbnailUrl = thumbnailUrl
)

fun Topic.toEntity() = TopicEntity(
    id = id,
    name = name,
    thumbnailUrl = thumbnailUrl
)

