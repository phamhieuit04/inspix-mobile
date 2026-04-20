package com.example.inspixmobile.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.paging.PagingSource
import androidx.room.Query
import androidx.room.Transaction
import com.example.inspixmobile.data.source.local.entity.CollectionEntity
import com.example.inspixmobile.data.source.local.relationship.CollectionWithImages
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Transaction
    @Query("SELECT * FROM collections")
    fun getCollectionsWithImages(): Flow<List<CollectionWithImages>>

    @Transaction
    @Query("SELECT * FROM collections ORDER BY uuid DESC")
    fun getPagingCollectionsWithImages(): PagingSource<Int, CollectionWithImages>

    @Query("SELECT COUNT(*) FROM collections")
    suspend fun countCollections(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(collections: List<CollectionEntity>)

    @Query("DELETE FROM collections")
    suspend fun clearAll()
}




