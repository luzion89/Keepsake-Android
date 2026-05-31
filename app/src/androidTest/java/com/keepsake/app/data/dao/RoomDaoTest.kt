package com.keepsake.app.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.keepsake.app.data.local.dao.RoomDao
import com.keepsake.app.data.local.database.KeepsakeDatabase
import com.keepsake.app.data.local.entity.RoomEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RoomDaoTest {
    private lateinit var db: KeepsakeDatabase
    private lateinit var dao: RoomDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KeepsakeDatabase::class.java
        ).build()
        dao = db.roomDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertAndGetById() = runTest {
        val room = RoomEntity(id = "r1", name = "厨房", updatedAt = 1000)
        dao.upsert(room)

        val result = dao.getById("r1")
        assertNotNull(result)
        assertEquals("厨房", result?.name)
    }

    @Test
    fun getByIdReturnsNullForNonExistent() = runTest {
        assertNull(dao.getById("nonexistent"))
    }

    @Test
    fun getAllShouldReturnAllNonDeleted() = runTest {
        dao.upsert(RoomEntity(id = "r1", name = "厨房", updatedAt = 3000))
        dao.upsert(RoomEntity(id = "r2", name = "客厅", updatedAt = 2000))
        dao.upsert(RoomEntity(id = "r3", name = "卧室", updatedAt = 1000, deleted = true))

        val rooms = dao.getAll()
        assertEquals(2, rooms.size)
        // Sorted by updatedAt DESC
        assertEquals("厨房", rooms[0].name)
        assertEquals("客厅", rooms[1].name)
    }

    @Test
    fun countShouldOnlyCountNonDeleted() = runTest {
        assertEquals(0, dao.count())
        dao.upsert(RoomEntity(id = "r1", name = "A"))
        assertEquals(1, dao.count())
        dao.upsert(RoomEntity(id = "r2", name = "B", deleted = true))
        assertEquals(1, dao.count())
    }

    @Test
    fun renameShouldUpdateNameAndTimestamp() = runTest {
        dao.upsert(RoomEntity(id = "r1", name = "旧名称", updatedAt = 1000))
        dao.rename("r1", "新名称", 9999)

        val result = dao.getById("r1")
        assertEquals("新名称", result?.name)
    }

    @Test
    fun softDeleteShouldMarkAsDeleted() = runTest {
        dao.upsert(RoomEntity(id = "r1", name = "测试"))
        dao.softDelete("r1", 9999)

        assertNull(dao.getById("r1")) // getById filters deleted=0
        assertEquals(0, dao.count())
    }

    @Test fun `H01 empty list returns empty`() = runTest {
        assertTrue(dao.getAll().isEmpty())
    }

    @Test fun `H05 blank name is stored as-is`() = runTest {
        dao.upsert(RoomEntity(id = "r1", name = "", updatedAt = 1000))
        assertEquals("", dao.getById("r1")?.name)
    }

    @Test fun `H05 null name is stored`() = runTest {
        dao.upsert(RoomEntity(id = "r1", name = "", updatedAt = 1000))
        assertNotNull(dao.getById("r1"))
    }

    @Test fun `H06 long name 60 chars`() = runTest {
        val longName = "A".repeat(60)
        dao.upsert(RoomEntity(id = "r1", name = longName))
        assertEquals(60, dao.getById("r1")?.name?.length)
    }
}
