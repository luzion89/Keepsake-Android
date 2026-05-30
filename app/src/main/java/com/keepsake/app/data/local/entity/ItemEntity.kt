package com.keepsake.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "items",
    foreignKeys = [ForeignKey(
        entity = AreaEntity::class,
        parentColumns = ["id"],
        childColumns = ["area_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("updated_at"), Index("area_id"), Index("name")]
)
@Serializable
data class ItemEntity(
    @PrimaryKey val id: String = "",
    @ColumnInfo(name = "area_id") val areaId: String = "",
    val name: String = "",
    val qty: Int = 0,
    val unit: String? = null,
    @ColumnInfo(name = "tags", defaultValue = "[]")
    val tags: String = "[]",
    @ColumnInfo(name = "photo_ids", defaultValue = "[]")
    val photoIds: String = "[]",
    @ColumnInfo(name = "expires_at") val expiresAt: Long? = null,
    val source: String = "manual",
    val confidence: Double? = null,
    val bbox: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = 0L,
    @ColumnInfo(name = "updated_by") val updatedBy: String = "",
    val deleted: Boolean = false,
    val version: Int = 0
)
