package com.example.inspixmobile.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.inspixmobile.data.source.local.entity.CollectionEntity
import com.example.inspixmobile.data.source.local.relationship.CollectionWithImages
import com.example.inspixmobile.data.source.local.relationship.CollectionWithImagesAndAuthor
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Transaction
    @Query("SELECT * FROM collections ORDER BY rowid ASC")
    suspend fun getListCollectionsWithImagesAndAuthor(): List<CollectionWithImagesAndAuthor>

    @Transaction
    @Query("SELECT * FROM collections ORDER BY rowid ASC")
    fun getListCollectionsWithImagesFlow(): Flow<List<CollectionWithImages>>

    @Query("SELECT * FROM collections WHERE uuid = :uuid LIMIT 1")
    suspend fun findByUuid(uuid: String): CollectionWithImagesAndAuthor

    @Query("SELECT COUNT(*) FROM collections")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(collections: List<CollectionEntity>)

    @Transaction
    @Query("SELECT * FROM collections WHERE source = :source ORDER BY rowid ASC")
    fun observeCollectionsBySource(source: String): Flow<List<CollectionWithImages>>

    @Query("DELETE FROM collections WHERE source = :source")
    suspend fun clearBySource(source: String)

    @Query("DELETE FROM collections")
    suspend fun clearAll()

    @Query("UPDATE collections SET is_liked = :isLiked WHERE uuid = :uuid")
    suspend fun toggleLike(uuid: String, isLiked: Boolean)
}
