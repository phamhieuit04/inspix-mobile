package com.example.inspixmobile.data.source.local.db

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.inspixmobile.data.source.local.dao.CollectionDao
import com.example.inspixmobile.data.source.local.dao.ImageDao
import com.example.inspixmobile.data.source.local.dao.TopicDao
import com.example.inspixmobile.data.source.local.dao.UserDao
import com.example.inspixmobile.data.source.local.entity.CollectionEntity
import com.example.inspixmobile.data.source.local.entity.ImageEntity
import com.example.inspixmobile.data.source.local.entity.TopicEntity
import com.example.inspixmobile.data.source.local.entity.UserEntity
import java.time.LocalDateTime

class RoomConverters {
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
}

@Database(
    entities = [
        CollectionEntity::class,
        ImageEntity::class,
        UserEntity::class,
        TopicEntity::class
    ],
    version = 14,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun imageDao(): ImageDao
    abstract fun userDao(): UserDao
    abstract fun topicDao(): TopicDao
}