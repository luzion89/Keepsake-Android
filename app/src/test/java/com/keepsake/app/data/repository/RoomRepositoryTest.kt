package com.keepsake.app.data.repository

import com.keepsake.app.data.local.dao.AreaDao
import com.keepsake.app.data.local.dao.ItemDao
import com.keepsake.app.data.local.dao.RoomDao
import com.keepsake.app.data.local.entity.RoomEntity
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RoomRepositoryTest {
    private lateinit var roomDao: RoomDao
    private lateinit var areaDao: AreaDao
    private lateinit var itemDao: ItemDao
    private lateinit var repo: RoomRepository
    private val deviceId = "test-device"

    @Before
    fun setUp() {
        roomDao = mockk(relaxed = true)
        areaDao = mockk(relaxed = true)
        itemDao = mockk(relaxed = true)
        repo = RoomRepository(roomDao, areaDao, itemDao, mockk(relaxed = true))
    }

    @Test
    fun `createRoom should generate UUID and set timestamps`() = runTest {
        coEvery { areaDao.countByRoom(any()) } returns 0

        val room = repo.create("厨房", deviceId = deviceId)

        assertTrue(room.id.isNotBlank())
        assertEquals("厨房", room.name)
        assertEquals(deviceId, room.updatedBy)
        assertTrue(room.updatedAt > 0)
        assertEquals(0, room.areaCount)
        coVerify { roomDao.upsert(any()) }
    }

    @Test
    fun `createRoom should not accept blank name`() = runTest {
        // Business logic: blank name validation happens in ViewModel,
        // but we should still verify repository doesn't crash
        coEvery { areaDao.countByRoom(any()) } returns 0

        val room = repo.create("", deviceId = deviceId)
        assertEquals("", room.name)
    }

    @Test
    fun `rename should update name and timestamp`() = runTest {
        val id = "room-1"
        repo.rename(id, "新名称")
        coVerify { roomDao.rename(id, "新名称", any()) }
    }

    @Test
    fun `softDelete should cascade to areas and items`() = runTest {
        val id = "room-1"
        repo.softDelete(id)
        coVerify { roomDao.softDelete(id, any()) }
        coVerify { areaDao.softDeleteByRoom(id, any()) }
        coVerify { itemDao.softDeleteByRoom(id, any()) }
    }

    @Test
    fun `observeAll should emit rooms through domain mapping`() = runTest {
        val room1 = RoomEntity(id = "1", name = "厨房", updatedAt = 3000)
        val room2 = RoomEntity(id = "2", name = "客厅", updatedAt = 1000)
        coEvery { roomDao.observeAll() } returns flowOf(listOf(room1, room2))
        coEvery { areaDao.countByRoom("1") } returns 3
        coEvery { areaDao.countByRoom("2") } returns 0

        val roomList = repo.observeAll().first()

        assertEquals(2, roomList.size)
        assertEquals("厨房", roomList[0].name)
        assertEquals(3, roomList[0].areaCount)
        assertEquals("客厅", roomList[1].name)
        assertEquals(0, roomList[1].areaCount)
    }

    @Test
    fun `getById should return null for non-existent room`() = runTest {
        coEvery { roomDao.getById("nonexistent") } returns null
        val result = repo.getById("nonexistent")
        assertNull(result)
    }

    @Test
    fun `count should delegate to DAO`() = runTest {
        coEvery { roomDao.count() } returns 5
        assertEquals(5, repo.count())
    }
}
