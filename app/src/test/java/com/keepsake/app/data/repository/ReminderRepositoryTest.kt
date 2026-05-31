package com.keepsake.app.data.repository

import com.keepsake.app.data.local.dao.ItemDao
import com.keepsake.app.data.local.dao.ReminderRuleDao
import com.keepsake.app.data.local.entity.ItemEntity
import com.keepsake.app.data.local.entity.ReminderRuleEntity
import com.keepsake.app.domain.model.Item
import com.keepsake.app.domain.model.ReminderRule
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class ReminderRepositoryTest {
    private lateinit var reminderDao: ReminderRuleDao
    private lateinit var itemDao: ItemDao
    private lateinit var repo: ReminderRepository

    @Before fun setUp() {
        reminderDao = mockk(relaxed = true)
        itemDao = mockk(relaxed = true)
        repo = ReminderRepository(reminderDao, itemDao)
    }

    @Test fun `RM02 create expiry reminder`() = runTest {
        val rule = repo.create(itemId = "i1", kind = "expiry", thresholdAt = TimeUnit.DAYS.toMillis(30))
        assertEquals("i1", rule.itemId)
        assertEquals("expiry", rule.kind)
        assertEquals(TimeUnit.DAYS.toMillis(30), rule.thresholdAt)
        assertNull(rule.lastFiredAt)
    }

    @Test fun `RM03 create low_stock reminder`() = runTest {
        val rule = repo.create(itemId = "i2", kind = "low_stock", thresholdQty = 3)
        assertEquals("low_stock", rule.kind)
        assertEquals(3, rule.thresholdQty)
    }

    @Test fun `RM04 create recheck reminder`() = runTest {
        val rule = repo.create(itemId = "i3", kind = "recheck", thresholdAt = TimeUnit.DAYS.toMillis(90))
        assertEquals("recheck", rule.kind)
    }

    @Test fun `RM06 update lastFiredAt after dismiss`() = runTest {
        repo.updateFired("r1")
        coVerify { reminderDao.updateFired("r1", any(), any()) }
    }

    @Test fun `RM07 get reminders by item`() = runTest {
        val entities = listOf(
            ReminderRuleEntity(id = "r1", itemId = "i1", kind = "expiry"),
            ReminderRuleEntity(id = "r2", itemId = "i1", kind = "low_stock")
        )
        coEvery { reminderDao.getByItem("i1") } returns entities
        assertEquals(2, repo.getByItem("i1").size)
    }

    @Test fun `soft delete reminder`() = runTest {
        repo.softDelete("r1")
        coVerify { reminderDao.softDelete("r1", any()) }
    }

    @Test fun `getAll returns all non-deleted reminders`() = runTest {
        val entities = listOf(
            ReminderRuleEntity(id = "r1", itemId = "i1", kind = "expiry"),
            ReminderRuleEntity(id = "r2", itemId = "i2", kind = "low_stock")
        )
        coEvery { reminderDao.getAll() } returns entities
        assertEquals(2, repo.getAll().size)
    }
}
