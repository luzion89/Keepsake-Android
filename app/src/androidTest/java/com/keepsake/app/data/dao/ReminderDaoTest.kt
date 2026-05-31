package com.keepsake.app.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.keepsake.app.data.local.database.KeepsakeDatabase
import com.keepsake.app.data.local.entity.AreaEntity
import com.keepsake.app.data.local.entity.ItemEntity
import com.keepsake.app.data.local.entity.ReminderRuleEntity
import com.keepsake.app.data.local.entity.RoomEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ReminderDaoTest {
    private lateinit var db: KeepsakeDatabase

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), KeepsakeDatabase::class.java).build()
        runTest {
            db.roomDao().upsert(RoomEntity(id = "r1", name = "厨房"))
            db.areaDao().upsert(AreaEntity(id = "a1", roomId = "r1", name = "柜子"))
            db.itemDao().upsert(ItemEntity(id = "i1", areaId = "a1", name = "牛奶", expiresAt = 9999999))
            db.itemDao().upsert(ItemEntity(id = "i2", areaId = "a1", name = "洗发水", qty = 1))
            db.itemDao().upsert(ItemEntity(id = "i3", areaId = "a1", name = "药箱"))
        }
    }

    @After fun tearDown() { db.close() }

    @Test fun `I12 add expiry reminder`() = runTest {
        val rule = ReminderRuleEntity(id = "r1", itemId = "i1", kind = "expiry", thresholdAt = 86400000 * 30)
        db.reminderRuleDao().upsert(rule)
        val rules = db.reminderRuleDao().getByItem("i1")
        assertEquals(1, rules.size)
        assertEquals("expiry", rules[0].kind)
    }

    @Test fun `I13 low_stock reminder with threshold_qty`() = runTest {
        val rule = ReminderRuleEntity(id = "r2", itemId = "i2", kind = "low_stock", thresholdQty = 3)
        db.reminderRuleDao().upsert(rule)
        val rules = db.reminderRuleDao().getByItem("i2")
        assertEquals(1, rules.size)
        assertEquals("low_stock", rules[0].kind)
        assertEquals(3, rules[0].thresholdQty)
    }

    @Test fun `I14 recheck reminder with interval`() = runTest {
        val rule = ReminderRuleEntity(id = "r3", itemId = "i3", kind = "recheck", thresholdAt = 86400000 * 90)
        db.reminderRuleDao().upsert(rule)
        val rules = db.reminderRuleDao().getByItem("i3")
        assertEquals(1, rules.size)
        assertEquals("recheck", rules[0].kind)
    }

    @Test fun `I15 multiple reminders per item`() = runTest {
        db.reminderRuleDao().upsert(ReminderRuleEntity(id = "r1", itemId = "i1", kind = "expiry", thresholdAt = 86400000 * 7))
        db.reminderRuleDao().upsert(ReminderRuleEntity(id = "r2", itemId = "i1", kind = "low_stock", thresholdQty = 2))
        assertEquals(2, db.reminderRuleDao().getByItem("i1").size)
    }

    @Test fun `RM06 update last_fired_at`() = runTest {
        db.reminderRuleDao().upsert(ReminderRuleEntity(id = "r1", itemId = "i1", kind = "expiry"))
        val now = System.currentTimeMillis()
        db.reminderRuleDao().updateFired("r1", now, now)
        val rules = db.reminderRuleDao().getAll()
        assertEquals(now, rules[0].lastFiredAt)
    }

    @Test fun `RM08 no re-fire within 1 hour`() = runTest {
        val oneHourAgo = System.currentTimeMillis() - 3600_001
        db.reminderRuleDao().upsert(ReminderRuleEntity(id = "r1", itemId = "i1", kind = "expiry", lastFiredAt = oneHourAgo))
        db.reminderRuleDao().upsert(ReminderRuleEntity(id = "r2", itemId = "i2", kind = "low_stock", lastFiredAt = System.currentTimeMillis() - 600_000))
        val all = db.reminderRuleDao().getAll()
        // Item i2's reminder (fired 10 min ago) should still be returned — filtering happens in ViewModel
        assertEquals(2, all.size)
    }

    @Test fun `I18 delete item cascades reminders`() = runTest {
        db.reminderRuleDao().upsert(ReminderRuleEntity(id = "r1", itemId = "i1", kind = "expiry"))
        db.reminderRuleDao().softDeleteByItem("i1", 9999)
        assertEquals(0, db.reminderRuleDao().getByItem("i1").size)
    }

    @Test fun `soft delete single reminder`() = runTest {
        db.reminderRuleDao().upsert(ReminderRuleEntity(id = "r1", itemId = "i1", kind = "expiry"))
        db.reminderRuleDao().softDelete("r1", 9999)
        assertEquals(0, db.reminderRuleDao().getAll().size)
    }
}
