package com.example.inspixmobile.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "likes")
data class LikeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long? = null,
    @ColumnInfo(name = "user_id")
    val userId: Long? = null,
    @ColumnInfo(name = "collection_id")
    val collectionId: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime? = null
)

