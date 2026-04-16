package com.example.inspixmobile.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long? = null,
    @ColumnInfo(name = "user_id")
    val userId: Long? = null,
    @ColumnInfo(name = "title")
    val title: String? = null,
    @ColumnInfo(name = "description")
    val description: String? = null,
    @ColumnInfo(name = "topic_id")
    val topicId: Int? = null,
    @ColumnInfo(name = "total_likes")
    val totalLikes: Int? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime? = null,
)
