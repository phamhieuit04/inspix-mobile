package com.example.inspixmobile.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey
    @ColumnInfo(name = "label")
    val label: String,
    @ColumnInfo(name = "next_offset")
    val nextOffset: Int
)