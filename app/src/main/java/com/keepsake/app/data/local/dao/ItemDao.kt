package com.keepsake.app.data.local.dao

import androidx.room.*
import com.keepsake.app.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE deleted = 0 ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE area_id = :areaId AND deleted = 0 ORDER BY updated_at DESC")
    fun observeByArea(areaId: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE area_id = :areaId AND deleted = 0 ORDER BY updated_at DESC")
    suspend fun getByArea(areaId: String): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id = :id AND deleted = 0")
    suspend fun getById(id: String): ItemEntity?

    @Query("""
        SELECT * FROM items
        WHERE deleted = 0
        AND (name LIKE '%' || :query || '%'
             OR notes LIKE '%' || :query || '%'
             OR tags LIKE '%' || :query || '%')
        ORDER BY updated_at DESC
        LIMIT 100
    """)
    suspend fun search(query: String): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ItemEntity)

    @Query("UPDATE items SET name = :name, updated_at = :updatedAt, version = version + 1 WHERE id = :id")
    suspend fun rename(id: String, name: String, updatedAt: Long)

    @Query("UPDATE items SET qty = :qty, updated_at = :updatedAt, updated_by = :updatedBy, version = version + 1 WHERE id = :id")
    suspend fun updateQty(id: String, qty: Int, updatedAt: Long, updatedBy: String)

    @Query("UPDATE items SET qty = qty + :delta, updated_at = :updatedAt, updated_by = :updatedBy, version = version + 1 WHERE id = :id")
    suspend fun qtyDelta(id: String, delta: Int, updatedAt: Long, updatedBy: String)

    @Query("UPDATE items SET deleted = 1, updated_at = :updatedAt, version = version + 1 WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE items SET deleted = 1, updated_at = :updatedAt, version = version + 1 WHERE area_id = :areaId")
    suspend fun softDeleteByArea(areaId: String, updatedAt: Long)

    @Query("""
        UPDATE items SET deleted = 1, updated_at = :updatedAt, version = version + 1
        WHERE area_id IN (SELECT id FROM areas WHERE room_id = :roomId)
    """)
    suspend fun softDeleteByRoom(roomId: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM items WHERE area_id = :areaId AND deleted = 0")
    suspend fun countByArea(areaId: String): Int

    @Query("SELECT COUNT(*) FROM items WHERE deleted = 0")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM items WHERE deleted = 0 AND expires_at IS NOT NULL AND expires_at < :threshold")
    suspend fun countExpiringSoon(threshold: Long): Int
}
