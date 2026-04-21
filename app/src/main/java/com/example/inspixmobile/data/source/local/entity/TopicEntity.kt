package com.example.inspixmobile.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int? = null,
    @ColumnInfo(name = "name")
    val name: String? = null
)

