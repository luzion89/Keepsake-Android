package com.keepsake.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "snapshots",
    foreignKeys = [ForeignKey(
        entity = AreaEntity::class,
        parentColumns = ["id"],
        childColumns = ["area_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("updated_at"), Index("area_id")]
)
@Serializable
data class SnapshotEntity(
    @PrimaryKey val id: String = "",
    @ColumnInfo(name = "area_id") val areaId: String = "",
    @ColumnInfo(name = "taken_at") val takenAt: Long = 0L,
    @ColumnInfo(name = "item_ids", defaultValue = "[]")
    val itemIds: String = "[]",
    val note: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = 0L,
    @ColumnInfo(name = "updated_by") val updatedBy: String = "",
    val deleted: Boolean = false,
    val version: Int = 0
)
