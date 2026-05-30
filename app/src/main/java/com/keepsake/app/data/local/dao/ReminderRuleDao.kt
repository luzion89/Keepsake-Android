package com.keepsake.app.data.local.dao

import androidx.room.*
import com.keepsake.app.data.local.entity.ReminderRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderRuleDao {
    @Query("SELECT * FROM reminder_rules WHERE deleted = 0 ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<ReminderRuleEntity>>

    @Query("SELECT * FROM reminder_rules WHERE item_id = :itemId AND deleted = 0")
    suspend fun getByItem(itemId: String): List<ReminderRuleEntity>

    @Query("SELECT * FROM reminder_rules WHERE deleted = 0")
    suspend fun getAll(): List<ReminderRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: ReminderRuleEntity)

    @Query("UPDATE reminder_rules SET last_fired_at = :firedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateFired(id: String, firedAt: Long, updatedAt: Long)

    @Query("UPDATE reminder_rules SET deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE reminder_rules SET deleted = 1, updated_at = :updatedAt WHERE item_id = :itemId")
    suspend fun softDeleteByItem(itemId: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM reminder_rules WHERE deleted = 0")
    suspend fun count(): Int
}
