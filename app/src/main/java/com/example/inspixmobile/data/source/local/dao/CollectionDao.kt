package com.example.inspixmobile.data.source.local.dao

import androidx.paging.PagingSource
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
    @Query("SELECT * FROM collections ORDER BY created_at ASC")
    suspend fun getListCollectionsWithImagesAndAuthor(): List<CollectionWithImagesAndAuthor>

    @Transaction
    @Query("SELECT * FROM collections ORDER BY created_at ASC")
    fun getListCollectionsWithImagesFlow(): Flow<List<CollectionWithImages>>

    @Transaction
    @Query("SELECT * FROM collections WHERE user_uuid != :userUuid AND is_liked = 1 ORDER BY created_at ASC")
    fun getLikedCollections(userUuid: String): Flow<List<CollectionWithImages>>

    @Transaction
    @Query("SELECT * FROM collections WHERE user_uuid = :userUuid ORDER BY created_at ASC")
    fun getOwnedCollections(userUuid: String): Flow<List<CollectionWithImages>>

    @Query("SELECT * FROM collections WHERE uuid = :uuid LIMIT 1")
    suspend fun findByUuid(uuid: String): CollectionWithImagesAndAuthor

    @Query("SELECT COUNT(*) FROM collections")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(collections: List<CollectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(collection: CollectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(collections: List<CollectionEntity>)

    @Query("UPDATE collections SET is_liked = 1 WHERE uuid = :uuid")
    suspend fun upsertLikedCollection(uuid: String)

    @Query("DELETE FROM collections")
    suspend fun clearAll()

    @Query("UPDATE collections SET is_liked = :isLiked WHERE uuid = :uuid")
    suspend fun toggleLike(uuid: String, isLiked: Boolean)

    @Query("UPDATE collections SET is_liked = 0")
    suspend fun resetLikedCollections()

    @Transaction
    @Query("SELECT * FROM collections WHERE is_liked = 1 ORDER BY created_at ASC")
    fun getLikedCollectionsPagingSource(): PagingSource<Int, CollectionWithImagesAndAuthor>

    @Transaction
    @Query("SELECT * FROM collections WHERE user_uuid = :userUuid ORDER BY created_at ASC")
    fun getOwnedCollectionsPagingSource(userUuid: String): PagingSource<Int, CollectionWithImagesAndAuthor>

    @Query("SELECT COUNT(*) FROM collections WHERE user_uuid != :userUuid AND is_liked = 1")
    suspend fun countLikedCollections(userUuid: String): Int

    @Query("SELECT COUNT(*) FROM collections WHERE user_uuid == :userUuid")
    suspend fun countCollectionsByUser(userUuid: String): Int

    @Query("DELETE FROM collections WHERE user_uuid != :userUuid AND is_liked = 1")
    suspend fun clearLikedCollections(userUuid: String)

    @Query("DELETE FROM collections WHERE user_uuid = :userUuid")
    suspend fun clearByUserUuid(userUuid: String)
}
