package com.keepsake.app.data.local.dao

import androidx.room.*
import com.keepsake.app.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE deleted = 0 ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE deleted = 0 ORDER BY updated_at DESC")
    suspend fun getAll(): List<RoomEntity>

    @Query("SELECT * FROM rooms WHERE id = :id AND deleted = 0")
    suspend fun getById(id: String): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(room: RoomEntity)

    @Query("UPDATE rooms SET name = :name, updated_at = :updatedAt, version = version + 1 WHERE id = :id")
    suspend fun rename(id: String, name: String, updatedAt: Long)

    @Query("UPDATE rooms SET deleted = 1, updated_at = :updatedAt, version = version + 1 WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM rooms WHERE deleted = 0")
    suspend fun count(): Int
}
