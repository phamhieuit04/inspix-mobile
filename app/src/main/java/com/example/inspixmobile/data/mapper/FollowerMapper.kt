package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.FollowerResponseDto
import com.example.inspixmobile.data.source.local.entity.FollowerEntity
import com.example.inspixmobile.domain.model.Follower

fun FollowerResponseDto.toDomain() = Follower(
    created = created
)

