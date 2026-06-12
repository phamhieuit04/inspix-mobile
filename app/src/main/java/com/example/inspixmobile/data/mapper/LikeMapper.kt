package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.LikeResponseDto
import com.example.inspixmobile.data.source.local.entity.LikeEntity
import com.example.inspixmobile.domain.model.Like

fun LikeResponseDto.toDomain() = Like(
    created = this.created ?: false,
    totalLikes = this.total_likes ?: 0
)
