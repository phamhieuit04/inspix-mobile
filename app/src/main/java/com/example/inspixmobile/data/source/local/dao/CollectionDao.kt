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

    @Transaction
    @Query("SELECT * FROM collections WHERE user_uuid != :userUuid AND is_liked = 1")
    fun getLikedCollections(userUuid: String): Flow<List<CollectionWithImages>>

    @Transaction
    @Query("SELECT * FROM collections WHERE user_uuid = :userUuid")
    fun getOwnedCollections(userUuid: String): Flow<List<CollectionWithImages>>

    @Query("SELECT * FROM collections WHERE uuid = :uuid LIMIT 1")
    suspend fun findByUuid(uuid: String): CollectionWithImagesAndAuthor

    @Query("SELECT COUNT(*) FROM collections")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(collections: List<CollectionEntity>)

    @Query("UPDATE collections SET is_liked = 1 WHERE uuid = :uuid")
    suspend fun upsertLikedCollection(uuid: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOwnedCollection(collection: CollectionEntity)

    @Query("DELETE FROM collections")
    suspend fun clearAll()

    @Query("UPDATE collections SET is_liked = :isLiked WHERE uuid = :uuid")
    suspend fun toggleLike(uuid: String, isLiked: Boolean)
}
