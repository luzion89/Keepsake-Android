package com.keepsake.app.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.keepsake.app.data.local.dao.AreaDao
import com.keepsake.app.data.local.dao.ItemDao
import com.keepsake.app.data.local.dao.RoomDao
import com.keepsake.app.data.local.database.KeepsakeDatabase
import com.keepsake.app.data.local.entity.AreaEntity
import com.keepsake.app.data.local.entity.ItemEntity
import com.keepsake.app.data.local.entity.RoomEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ItemDaoTest {
    private lateinit var db: KeepsakeDatabase
    private lateinit var itemDao: ItemDao
    private lateinit var roomDao: RoomDao
    private lateinit var areaDao: AreaDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KeepsakeDatabase::class.java
        ).build()
        itemDao = db.itemDao()
        roomDao = db.roomDao()
        areaDao = db.areaDao()

        // Setup test hierarchy
        runTest {
            roomDao.upsert(RoomEntity(id = "room1", name = "厨房"))
            areaDao.upsert(AreaEntity(id = "area1", roomId = "room1", name = "柜子"))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertAndGetById() = runTest {
        val item = ItemEntity(id = "i1", areaId = "area1", name = "洗发水", qty = 3)
        itemDao.upsert(item)

        val result = itemDao.getById("i1")
        assertNotNull(result)
        assertEquals("洗发水", result?.name)
        assertEquals(3, result?.qty)
    }

    @Test
    fun searchShouldMatchName() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "飘柔洗发水"))
        itemDao.upsert(ItemEntity(id = "i2", areaId = "area1", name = "高露洁牙膏"))
        itemDao.upsert(ItemEntity(id = "i3", areaId = "area1", name = "洗手液"))

        val results = itemDao.search("洗发")
        assertEquals(1, results.size)
        assertEquals("飘柔洗发水", results[0].name)
    }

    @Test
    fun searchShouldMatchNotes() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "沐浴露", notes = "白色，在牙刷旁边"))
        itemDao.upsert(ItemEntity(id = "i2", areaId = "area1", name = "牙膏", notes = "薄荷味"))

        val results = itemDao.search("牙刷")
        assertEquals(1, results.size)
        assertEquals("沐浴露", results[0].name)
    }

    @Test
    fun searchShouldMatchTags() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "洗发水", tags = "\"日用品\",\"浴室\""))
        itemDao.upsert(ItemEntity(id = "i2", areaId = "area1", name = "螺丝刀", tags = "\"工具\""))

        val results = itemDao.search("日用品")
        assertEquals(1, results.size)
        assertEquals("洗发水", results[0].name)
    }

    @Test
    fun qtyDeltaShouldIncreaseQuantity() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "测试", qty = 5))
        itemDao.qtyDelta("i1", 3, 9999, "test-device")

        val result = itemDao.getById("i1")
        assertEquals(8, result?.qty)
    }

    @Test
    fun qtyDeltaShouldDecreaseQuantity() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "测试", qty = 5))
        itemDao.qtyDelta("i1", -2, 9999, "test-device")

        val result = itemDao.getById("i1")
        assertEquals(3, result?.qty)
    }

    @Test
    fun softDeleteShouldHideItem() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "测试"))
        itemDao.softDelete("i1", 9999)

        assertNull(itemDao.getById("i1"))
        assertEquals(0, itemDao.countByArea("area1"))
    }

    @Test
    fun getByAreaShouldOnlyReturnNonDeleted() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "A"))
        itemDao.upsert(ItemEntity(id = "i2", areaId = "area1", name = "B", deleted = true))

        val items = itemDao.getByArea("area1")
        assertEquals(1, items.size)
        assertEquals("A", items[0].name)
    }

    @Test
    fun countExpiringSoonShouldCountCorrectly() = runTest {
        val now = System.currentTimeMillis()
        val futureExpiry = now + 86400000 * 10 // 10 days
        val nearExpiry = now + 86400000 * 2    // 2 days

        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "即将过期", expiresAt = nearExpiry))
        itemDao.upsert(ItemEntity(id = "i2", areaId = "area1", name = "还早", expiresAt = futureExpiry))
        itemDao.upsert(ItemEntity(id = "i3", areaId = "area1", name = "无过期"))

        val threshold = now + 86400000 * 7
        val count = itemDao.countExpiringSoon(threshold)
        assertEquals(1, count)
    }

    @Test fun `A05 empty name item can be created`() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = ""))
        assertEquals("", itemDao.getById("i1")?.name)
    }

    @Test fun `I05 unit field stores formats`() = runTest {
        listOf("瓶", "盒", "袋", "个", "包", "升", "毫升").forEach { unit ->
            itemDao.upsert(ItemEntity(id = "u_$unit", areaId = "area1", name = unit, unit = unit))
        }
        assertEquals("瓶", itemDao.getById("u_瓶")?.unit)
        assertEquals("毫升", itemDao.getById("u_毫升")?.unit)
    }

    @Test fun `I06 expiresAt stores values correctly`() = runTest {
        itemDao.upsert(ItemEntity(id = "i2", areaId = "area1", name = "有过期", expiresAt = 9999))
        assertEquals(9999, itemDao.getById("i2")?.expiresAt)

        itemDao.upsert(ItemEntity(id = "i3", areaId = "area1", name = "无过期"))
        assertNotNull(itemDao.getById("i3"))
    }

    @Test fun `I11 duplicate tags should be stored`() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "标签测试", tags = "\"日用\",\"日用\",\"工具\""))
        assertNotNull(itemDao.getById("i1"))
    }

    @Test fun `S02 search partial name match`() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "飘柔洗发水"))
        assertEquals(1, itemDao.search("飘柔").size)
        assertEquals(1, itemDao.search("发水").size)
        assertEquals(1, itemDao.search("飘柔洗发水").size)
    }

    @Test fun `S05 search no results returns empty`() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "洗发水"))
        assertTrue(itemDao.search("电视机").isEmpty())
    }

    @Test fun `I20 item with photos stores photoIds`() = runTest {
        itemDao.upsert(ItemEntity(id = "i1", areaId = "area1", name = "带照片", photoIds = "\"p1\",\"p2\""))
        val item = itemDao.getById("i1")
        assertNotNull(item)
        assertTrue(item?.photoIds?.contains("p1") == true)
    }
}
