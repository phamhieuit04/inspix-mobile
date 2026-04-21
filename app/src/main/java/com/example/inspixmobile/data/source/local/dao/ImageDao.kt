package com.example.inspixmobile.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.inspixmobile.data.source.local.entity.ImageEntity

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<ImageEntity>)

    @Query("DELETE FROM images")
    suspend fun clearAll()
}




