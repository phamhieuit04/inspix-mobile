package com.example.inspixmobile.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.local.dao.ImageDao
import com.example.inspixmobile.data.source.local.dao.RemoteKeyDao
import com.example.inspixmobile.data.source.local.entity.CollectionEntity
import com.example.inspixmobile.data.source.local.entity.ImageEntity
import com.example.inspixmobile.data.source.local.entity.RemoteKeyEntity
import java.time.LocalDateTime

class RoomConverters {
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
}

@Database(
    entities = [
        CollectionEntity::class,
        ImageEntity::class,
        RemoteKeyEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun imageDao(): ImageDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}