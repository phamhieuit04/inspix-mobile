package com.example.inspixmobile.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.inspixmobile.data.source.local.entity.CommentEntity
import com.example.inspixmobile.data.source.local.relationship.CommentWithUser
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE collection_uuid = :collectionUuid ORDER BY created_at DESC")
    fun getCommentsByCollectionUuid(collectionUuid: String): Flow<List<CommentWithUser>>

    @Query(" DELETE FROM comments WHERE collection_uuid = :collectionUuid")
    suspend fun deleteByCollectionUuid(collectionUuid: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<CommentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity)

    @Transaction
    suspend fun replaceComments(collectionUuid: String, comments: List<CommentEntity>) {
        deleteByCollectionUuid(collectionUuid)
        insertAll(comments)
    }
}