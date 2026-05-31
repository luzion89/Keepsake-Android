package com.keepsake.app.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.keepsake.app.data.local.database.KeepsakeDatabase
import com.keepsake.app.data.local.entity.AreaEntity
import com.keepsake.app.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AreaDaoTest {
    private lateinit var db: KeepsakeDatabase

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), KeepsakeDatabase::class.java).build()
        runTest {
            db.roomDao().upsert(RoomEntity(id = "r1", name = "厨房"))
            db.roomDao().upsert(RoomEntity(id = "r2", name = "客厅"))
        }
    }

    @After fun tearDown() { db.close() }

    @Test fun `R03 area list by room`() = runTest {
        db.areaDao().upsert(AreaEntity(id = "a1", roomId = "r1", name = "柜子"))
        db.areaDao().upsert(AreaEntity(id = "a2", roomId = "r1", name = "抽屉"))
        db.areaDao().upsert(AreaEntity(id = "a3", roomId = "r2", name = "电视柜"))

        val r1 = db.areaDao().getByRoom("r1")
        assertEquals(2, r1.size)
        val r2 = db.areaDao().getByRoom("r2")
        assertEquals(1, r2.size)
    }

    @Test fun `R04 area with no items should show count 0`() = runTest {
        db.areaDao().upsert(AreaEntity(id = "a1", roomId = "r1", name = "空区域"))
        assertEquals(0, db.itemDao().countByArea("a1"))
    }

    @Test fun `R06 area preset names should be storable`() = runTest {
        val presets = listOf("洗手台柜子", "墙壁柜", "抽屉", "沙发底下")
        presets.forEach { db.areaDao().upsert(AreaEntity(id = it.hashCode().toString(), roomId = "r1", name = it)) }
        assertEquals(4, db.areaDao().countByRoom("r1"))
    }

    @Test fun `R07 area rename updates name`() = runTest {
        db.areaDao().upsert(AreaEntity(id = "a1", roomId = "r1", name = "旧名"))
        db.areaDao().rename("a1", "新名", 9999)
        assertEquals("新名", db.areaDao().getById("a1")?.name)
    }

    @Test fun `R08 area soft delete`() = runTest {
        db.areaDao().upsert(AreaEntity(id = "a1", roomId = "r1", name = "删除测试"))
        db.areaDao().softDelete("a1", 9999)
        assertNull(db.areaDao().getById("a1"))
    }

    @Test fun `D02 area delete cascades to items`() = runTest {
        db.areaDao().upsert(AreaEntity(id = "a1", roomId = "r1", name = "带物品区域"))
        db.itemDao().upsert(com.keepsake.app.data.local.entity.ItemEntity(id = "i1", areaId = "a1", name = "物品1"))
        db.areaDao().softDelete("a1", 9999)
        db.itemDao().softDeleteByArea("a1", 9999)
        assertEquals(0, db.itemDao().countByArea("a1"))
    }

    @Test fun `count only non-deleted areas`() = runTest {
        db.areaDao().upsert(AreaEntity(id = "a1", roomId = "r1", name = "A"))
        db.areaDao().upsert(AreaEntity(id = "a2", roomId = "r1", name = "B", deleted = true))
        assertEquals(1, db.areaDao().count())
    }
}
