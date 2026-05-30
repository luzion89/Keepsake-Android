package com.keepsake.app.data.local.dao

import androidx.room.*
import com.keepsake.app.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE deleted = 0 ORDER BY taken_at DESC")
    fun observeAll(): Flow<List<PhotoEntity>>

    @Query("""
        SELECT * FROM photos
        WHERE parent_type = :parentType AND parent_id = :parentId AND deleted = 0
        ORDER BY taken_at DESC
    """)
    fun observeByParent(parentType: String, parentId: String): Flow<List<PhotoEntity>>

    @Query("""
        SELECT * FROM photos
        WHERE parent_type = :parentType AND parent_id = :parentId AND deleted = 0
        ORDER BY taken_at DESC
    """)
    suspend fun getByParent(parentType: String, parentId: String): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE id = :id AND deleted = 0")
    suspend fun getById(id: String): PhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(photo: PhotoEntity)

    @Query("UPDATE photos SET deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM photos WHERE deleted = 0")
    suspend fun count(): Int

    @Query("SELECT * FROM photos WHERE deleted = 0 AND blob_uri NOT NULL")
    suspend fun getAllWithBlobs(): List<PhotoEntity>

    @Query("SELECT blob_uri FROM photos WHERE deleted = 0 AND blob_uri NOT NULL")
    suspend fun getAllBlobUris(): List<String>
}
