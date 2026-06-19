package com.example.inspixmobile.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.inspixmobile.data.source.local.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics")
    fun getAll(): Flow<List<TopicEntity>>

    @Upsert
    suspend fun upsertAll(topics: List<TopicEntity>)

    @Query("DELETE FROM topics")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM topics")
    suspend fun count(): Int
}