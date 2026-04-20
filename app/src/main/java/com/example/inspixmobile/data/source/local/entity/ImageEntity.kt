package com.example.inspixmobile.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String = "",
    @ColumnInfo(name = "color")
    val color: String? = null,
    @ColumnInfo(name = "url_small")
    val urlSmall: String? = null,
    @ColumnInfo(name = "url_regular")
    val urlRegular: String? = null,
    @ColumnInfo(name = "url_full")
    val urlFull: String? = null,
    @ColumnInfo(name = "user_id")
    val userId: Long? = null,
    @ColumnInfo(name = "collection_id")
    val collectionId: Long? = null,
    @ColumnInfo(name = "download_url")
    val downloadUrl: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    @ColumnInfo(name = "created_at_human")
    val createdAtHuman: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null,
    @ColumnInfo(name = "updated_at_human")
    val updatedAtHuman: String? = null
)

