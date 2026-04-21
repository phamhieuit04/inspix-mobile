package com.example.inspixmobile.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.inspixmobile.data.source.local.entity.RemoteKeyEntity

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remoteKey: RemoteKeyEntity)

    @Query("SELECT * FROM remote_keys WHERE label = :label")
    suspend fun getByLabel(label: String): RemoteKeyEntity?

    @Query("DELETE FROM remote_keys WHERE label = :label")
    suspend fun deleteByLabel(label: String)
}