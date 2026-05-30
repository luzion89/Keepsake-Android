package com.keepsake.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "photos",
    indices = [Index("updated_at"), Index("parent_type", "parent_id")]
)
@Serializable
data class PhotoEntity(
    @PrimaryKey val id: String = "",
    @ColumnInfo(name = "parent_type") val parentType: String = "",
    @ColumnInfo(name = "parent_id") val parentId: String = "",
    @ColumnInfo(name = "taken_at") val takenAt: Long = 0L,
    @ColumnInfo(name = "blob_uri") val blobUri: String? = null,
    @ColumnInfo(name = "recognition_status", defaultValue = "pending")
    val recognitionStatus: String = "pending",
    @ColumnInfo(name = "recognition_result") val recognitionResult: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = 0L,
    @ColumnInfo(name = "updated_by") val updatedBy: String = "",
    val deleted: Boolean = false,
    val version: Int = 0
)
