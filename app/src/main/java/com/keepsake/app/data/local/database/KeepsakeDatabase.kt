package com.keepsake.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.keepsake.app.data.local.dao.*
import com.keepsake.app.data.local.entity.*

@Database(
    entities = [
        RoomEntity::class,
        AreaEntity::class,
        ItemEntity::class,
        PhotoEntity::class,
        SnapshotEntity::class,
        ReminderRuleEntity::class,
        KvEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KeepsakeDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun areaDao(): AreaDao
    abstract fun itemDao(): ItemDao
    abstract fun photoDao(): PhotoDao
    abstract fun reminderRuleDao(): ReminderRuleDao
    abstract fun kvDao(): KvDao
}
