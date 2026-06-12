package com.example.inspixmobile.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.inspixmobile.data.source.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Upsert
    suspend fun insertAll(users: List<UserEntity>)

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearAll()

    @Query("DELETE FROM users WHERE uuid != :uuid")
    suspend fun clearExcept(uuid: String)

    @Query("SELECT * FROM users WHERE uuid = :uuid LIMIT 1")
    fun observeUser(uuid: String): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM users WHERE is_followed = 1")
    fun countFollowedUsers(): Int
}