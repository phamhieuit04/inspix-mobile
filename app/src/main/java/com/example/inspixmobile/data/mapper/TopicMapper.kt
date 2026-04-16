package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.response.TopicResponseDto
import com.example.inspixmobile.data.entity.TopicEntity
import com.example.inspixmobile.domain.model.Topic

fun TopicResponseDto.toDomain() = Topic(
    id = id,
    name = name
)

fun TopicEntity.toDomain() = Topic(
    id = id,
    name = name
)

fun Topic.toEntity() = TopicEntity(
    id = id,
    name = name
)

