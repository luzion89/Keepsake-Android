package com.keepsake.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kv")
data class KvEntity(
    @PrimaryKey val key: String,
    val value: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = 0L
)
