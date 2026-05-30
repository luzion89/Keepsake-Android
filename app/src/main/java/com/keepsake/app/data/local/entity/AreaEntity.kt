package com.keepsake.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "areas",
    foreignKeys = [ForeignKey(
        entity = RoomEntity::class,
        parentColumns = ["id"],
        childColumns = ["room_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("updated_at"), Index("room_id")]
)
@Serializable
data class AreaEntity(
    @PrimaryKey val id: String = "",
    @ColumnInfo(name = "room_id") val roomId: String = "",
    val name: String = "",
    @ColumnInfo(name = "photo_ids", defaultValue = "[]")
    val photoIds: String = "[]",
    val note: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = 0L,
    @ColumnInfo(name = "updated_by") val updatedBy: String = "",
    val deleted: Boolean = false,
    val version: Int = 0
)
