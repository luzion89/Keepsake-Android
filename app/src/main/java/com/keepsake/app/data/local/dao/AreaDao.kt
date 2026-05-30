package com.keepsake.app.data.local.dao

import androidx.room.*
import com.keepsake.app.data.local.entity.AreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {
    @Query("SELECT * FROM areas WHERE deleted = 0 ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<AreaEntity>>

    @Query("SELECT * FROM areas WHERE room_id = :roomId AND deleted = 0 ORDER BY updated_at DESC")
    fun observeByRoom(roomId: String): Flow<List<AreaEntity>>

    @Query("SELECT * FROM areas WHERE room_id = :roomId AND deleted = 0 ORDER BY updated_at DESC")
    suspend fun getByRoom(roomId: String): List<AreaEntity>

    @Query("SELECT * FROM areas WHERE id = :id AND deleted = 0")
    suspend fun getById(id: String): AreaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(area: AreaEntity)

    @Query("UPDATE areas SET name = :name, updated_at = :updatedAt, version = version + 1 WHERE id = :id")
    suspend fun rename(id: String, name: String, updatedAt: Long)

    @Query("UPDATE areas SET deleted = 1, updated_at = :updatedAt, version = version + 1 WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE areas SET deleted = 1, updated_at = :updatedAt, version = version + 1 WHERE room_id = :roomId")
    suspend fun softDeleteByRoom(roomId: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM areas WHERE room_id = :roomId AND deleted = 0")
    suspend fun countByRoom(roomId: String): Int

    @Query("SELECT COUNT(*) FROM areas WHERE deleted = 0")
    suspend fun count(): Int
}
