package com.keepsake.app.data.repository

import com.keepsake.app.data.local.dao.ItemDao
import com.keepsake.app.data.local.dao.ReminderRuleDao
import com.keepsake.app.data.local.entity.ItemEntity
import com.keepsake.app.domain.model.Item
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ItemRepositoryTest {
    private lateinit var itemDao: ItemDao
    private lateinit var reminderDao: ReminderRuleDao
    private lateinit var repo: ItemRepository
    private val deviceId = "test-device"

    @Before
    fun setUp() {
        itemDao = mockk(relaxed = true)
        reminderDao = mockk(relaxed = true)
        repo = ItemRepository(itemDao, reminderDao)
    }

    @Test
    fun `create should set source and timestamps`() = runTest {
        val item = repo.create(
            areaId = "area-1",
            name = "洗发水",
            qty = 3,
            unit = "瓶",
            notes = "白色，在浴室",
            source = "ai",
            deviceId = deviceId
        )

        assertEquals("洗发水", item.name)
        assertEquals(3, item.qty)
        assertEquals("瓶", item.unit)
        assertEquals("白色，在浴室", item.notes)
        assertEquals("ai", item.source)
        assertEquals(deviceId, item.updatedBy)
        assertTrue(item.id.isNotBlank())
        assertTrue(item.createdAt > 0)
        coVerify { itemDao.upsert(any()) }
    }

    @Test
    fun `create with tags should save them`() = runTest {
        val item = repo.create(
            areaId = "area-1",
            name = "洗发水",
            tags = listOf("日用品", "浴室")
        )
        assertEquals(listOf("日用品", "浴室"), item.tags)
    }

    @Test
    fun `create with expiry should set expiresAt`() = runTest {
        val expiry = System.currentTimeMillis() + 86400000 * 30
        val item = repo.create(
            areaId = "area-1",
            name = "牛奶",
            expiresAt = expiry
        )
        assertEquals(expiry, item.expiresAt)
    }

    @Test
    fun `qtyDelta positive should increase quantity`() = runTest {
        repo.qtyDelta("item-1", 1, deviceId)
        coVerify { itemDao.qtyDelta("item-1", 1, any(), deviceId) }
    }

    @Test
    fun `qtyDelta negative should decrease quantity`() = runTest {
        repo.qtyDelta("item-1", -1, deviceId)
        coVerify { itemDao.qtyDelta("item-1", -1, any(), deviceId) }
    }

    @Test
    fun `softDelete should cascade to reminder rules`() = runTest {
        repo.softDelete("item-1")
        coVerify { itemDao.softDelete("item-1", any()) }
        coVerify { reminderDao.softDeleteByItem("item-1", any()) }
    }

    @Test
    fun `search should delegate to DAO`() = runTest {
        coEvery { itemDao.search("牙刷") } returns emptyList()
        val result = repo.search("牙刷")
        assertTrue(result.isEmpty())
        coVerify { itemDao.search("牙刷") }
    }

    @Test
    fun `update should overwrite all fields`() = runTest {
        val item = Item(
            id = "item-1",
            areaId = "area-1",
            name = "更新后",
            qty = 5,
            unit = "盒",
            notes = "更新备注",
            tags = listOf("tag1"),
            expiresAt = 9999L,
            source = "manual"
        )
        repo.update(item, deviceId)
        coVerify { itemDao.upsert(match { it.name == "更新后" && it.qty == 5 && it.unit == "盒" }) }
    }

    @Test
    fun `getById should return null when not found`() = runTest {
        coEvery { itemDao.getById("nonexistent") } returns null
        assertNull(repo.getById("nonexistent"))
    }

    @Test
    fun `parseJsonArray should handle various formats`() {
        assertEquals(listOf("a", "b"), parseJsonArray("""["a","b"]"""))
        assertEquals(emptyList<String>(), parseJsonArray("[]"))
        assertEquals(emptyList<String>(), parseJsonArray(""))
        assertEquals(emptyList<String>(), parseJsonArray("   "))
    }
}
