package com.example.inspixmobile.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String = "",
    @ColumnInfo(name = "user_uuid")
    val userUuid: String? = null,
    @ColumnInfo(name = "title")
    val title: String? = null,
    @ColumnInfo(name = "description")
    val description: String? = null,
    @ColumnInfo(name = "topic_id")
    val topicId: Int? = null,
    @ColumnInfo(name = "topic_name")
    val topicName: String? = null,
    @ColumnInfo(name = "is_liked")
    val isLiked: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime? = null,
    @ColumnInfo(name = "created_at_human")
    val createdAtHuman: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime? = null,
    @ColumnInfo(name = "updated_at_human")
    val updatedAtHuman: String? = null,
    @ColumnInfo(name = "total_likes")
    val totalLikes: Int? = null,
    @ColumnInfo(name = "total_comments")
    val totalComments: Int? = null
)

