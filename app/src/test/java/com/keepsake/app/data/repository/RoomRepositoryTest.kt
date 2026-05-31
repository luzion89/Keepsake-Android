package com.keepsake.app.data.repository

import org.junit.Assert.*
import org.junit.Test

class RoomRepositoryTest {

    @Test
    fun `parseJsonArray should parse valid JSON array`() {
        val result = parseJsonArray("""["a","b","c"]""")
        assertEquals(3, result.size)
        assertEquals("a", result[0])
        assertEquals("b", result[1])
        assertEquals("c", result[2])
    }

    @Test
    fun `parseJsonArray should return empty for empty array`() {
        val result = parseJsonArray("[]")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseJsonArray should return empty for blank string`() {
        assertTrue(parseJsonArray("").isEmpty())
        assertTrue(parseJsonArray("  ").isEmpty())
    }
}
