package com.example.inspixmobile.data.mapper

import com.example.inspixmobile.data.dto.UserInterestedDto
import com.example.inspixmobile.data.entity.UserInterestedEntity
import com.example.inspixmobile.data.model.UserInterested

fun UserInterestedDto.toDomain() = UserInterested(
    id = checkNotNull(id) { "id is required" },
    userId = checkNotNull(user_id) { "user_id is required" },
    topicIds = topic_ids.toTopicIdList(),
    createdAt = checkNotNull(created_at) { "created_at is required" },
    updatedAt = checkNotNull(updated_at) { "updated_at is required" }
)

fun UserInterested.toDto() = UserInterestedDto(
    id = id,
    user_id = userId,
    topic_ids = topicIds.joinToString(","),
    created_at = createdAt,
    updated_at = updatedAt
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
    topicIds = topicIds.joinToString(","),
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun String?.toTopicIdList(): List<Int> {
    if (this.isNullOrBlank()) return emptyList()
    return split(",").mapNotNull { part ->
        part.trim().toIntOrNull()
    }
}


