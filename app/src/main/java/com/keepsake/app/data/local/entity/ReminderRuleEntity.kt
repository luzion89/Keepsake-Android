package com.keepsake.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "reminder_rules",
    foreignKeys = [ForeignKey(
        entity = ItemEntity::class,
        parentColumns = ["id"],
        childColumns = ["item_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("updated_at"), Index("item_id")]
)
@Serializable
data class ReminderRuleEntity(
    @PrimaryKey val id: String = "",
    @ColumnInfo(name = "item_id") val itemId: String = "",
    val kind: String = "", // expiry, low_stock, recheck
    @ColumnInfo(name = "threshold_at") val thresholdAt: Long? = null,
    @ColumnInfo(name = "threshold_qty") val thresholdQty: Int? = null,
    val note: String? = null,
    @ColumnInfo(name = "last_fired_at") val lastFiredAt: Long? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = 0L,
    @ColumnInfo(name = "updated_by") val updatedBy: String = "",
    val deleted: Boolean = false,
    val version: Int = 0
)
