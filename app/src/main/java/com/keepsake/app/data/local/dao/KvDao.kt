package com.keepsake.app.data.local.dao

import androidx.room.*
import com.keepsake.app.data.local.entity.KvEntity

@Dao
interface KvDao {
    @Query("SELECT value FROM kv WHERE key = :key")
    suspend fun get(key: String): String?

    @Query("SELECT * FROM kv WHERE key = :key")
    suspend fun getEntity(key: String): KvEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(entry: KvEntity)
}
