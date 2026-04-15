package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.TopicDto
import com.example.inspixmobile.data.entity.TopicEntity
import com.example.inspixmobile.data.model.Topic

fun TopicDto.toDomain() = Topic(
    id = id,
    name = name
)

fun Topic.toDto() = TopicDto(
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

