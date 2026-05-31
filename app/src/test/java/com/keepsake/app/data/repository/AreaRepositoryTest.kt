package com.keepsake.app.data.repository

import com.keepsake.app.data.local.dao.AreaDao
import com.keepsake.app.data.local.dao.ItemDao
import com.keepsake.app.data.local.entity.AreaEntity
import com.keepsake.app.data.local.entity.ItemEntity
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AreaRepositoryTest {
    private lateinit var areaDao: AreaDao
    private lateinit var itemDao: ItemDao
    private lateinit var repo: AreaRepository

    @Before fun setUp() {
        areaDao = mockk(relaxed = true)
        itemDao = mockk(relaxed = true)
        repo = AreaRepository(areaDao, itemDao)
    }

    @Test fun `create area with name`() = runTest {
        coEvery { itemDao.countByArea(any()) } returns 0
        val area = repo.create("r1", "柜子")
        assertEquals("柜子", area.name)
        assertEquals("r1", area.roomId)
        assertTrue(area.id.isNotBlank())
        assertEquals(0, area.itemCount)
    }

    @Test fun `create area with note`() = runTest {
        coEvery { itemDao.countByArea(any()) } returns 0
        val area = repo.create("r1", "柜子", note = "白色衣柜")
        assertEquals("白色衣柜", area.note)
    }

    @Test fun `R05 empty name should still create`() = runTest {
        coEvery { itemDao.countByArea(any()) } returns 0
        val area = repo.create("r1", "")
        assertEquals("", area.name)
    }

    @Test fun `get by room returns empty for no areas`() = runTest {
        coEvery { areaDao.getByRoom("empty") } returns emptyList()
        val areas = repo.getByRoom("empty")
        assertTrue(areas.isEmpty())
    }

    @Test fun `getById returns null for missing area`() = runTest {
        coEvery { areaDao.getById("none") } returns null
        assertNull(repo.getById("none"))
    }

    @Test fun `rename area updates name`() = runTest {
        repo.rename("a1", "新名称")
        coVerify { areaDao.rename("a1", "新名称", any()) }
    }

    @Test fun `soft delete area cascades to items`() = runTest {
        repo.softDelete("a1")
        coVerify { areaDao.softDelete("a1", any()) }
        coVerify { itemDao.softDeleteByArea("a1", any()) }
    }

    @Test fun `observe by room emits areas via flow`() = runTest {
        val entities = listOf(AreaEntity(id = "a1", roomId = "r1", name = "A"), AreaEntity(id = "a2", roomId = "r1", name = "B"))
        coEvery { areaDao.observeByRoom("r1") } returns flowOf(entities)
        coEvery { itemDao.countByArea("a1") } returns 3
        coEvery { itemDao.countByArea("a2") } returns 0

        val areas = repo.observeByRoom("r1").first()
        assertEquals(2, areas.size)
        assertEquals("A", areas[0].name)
        assertEquals(3, areas[0].itemCount)
        assertEquals("B", areas[1].name)
        assertEquals(0, areas[1].itemCount)
    }
}
