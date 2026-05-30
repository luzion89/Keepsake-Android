package com.keepsake.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "rooms",
    indices = [Index("updated_at"), Index("name")]
)
@Serializable
data class RoomEntity(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val icon: String? = null,
    @ColumnInfo(name = "photo_ids", defaultValue = "[]")
    val photoIds: String = "[]",
    val note: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = 0L,
    @ColumnInfo(name = "updated_by") val updatedBy: String = "",
    val deleted: Boolean = false,
    val version: Int = 0
)
