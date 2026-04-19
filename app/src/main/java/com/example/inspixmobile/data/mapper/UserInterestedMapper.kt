package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.source.remote.dto.UserInterestedResponseDto
import com.example.inspixmobile.data.source.local.entity.UserInterestedEntity
import com.example.inspixmobile.domain.model.UserInterested

fun UserInterestedResponseDto.toDomain() = UserInterested(
    id = checkNotNull(id) { "id is required" },
    userId = checkNotNull(user_id) { "user_id is required" },
    topicIds = topic_ids.toTopicIdList(),
    createdAt = checkNotNull(created_at) { "created_at is required" },
    updatedAt = checkNotNull(updated_at) { "updated_at is required" }
)

fun UserInterestedEntity.toDomain() = UserInterested(
    id = checkNotNull(id) { "id is required" },
    userId = checkNotNull(userId) { "user_id is required" },
    topicIds = topicIds.toTopicIdList(),
    createdAt = checkNotNull(createdAt) { "created_at is required" },
    updatedAt = checkNotNull(updatedAt) { "updated_at is required" }
)

fun UserInterested.toEntity() = UserInterestedEntity(
    id = id,
    userId = userId,
    topicIds = topicIds?.joinToString(","),
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun String?.toTopicIdList(): List<Int> {
    if (this.isNullOrBlank()) return emptyList()
    return split(",").mapNotNull { part ->
        part.trim().toIntOrNull()
    }
}


