package com.example.inspixmobile.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.inspixmobile.data.source.local.entity.ImageEntity

@Dao
interface ImageDao {
    @Upsert
    suspend fun upsertAll(images: List<ImageEntity>)

    @Query("DELETE FROM images")
    suspend fun clearAll()
}




