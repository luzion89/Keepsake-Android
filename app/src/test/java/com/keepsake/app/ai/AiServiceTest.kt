package com.keepsake.app.ai

import org.junit.Assert.*
import org.junit.Test

class AiServiceTest {

    @Test
    fun `ParsedItem default values should be correct`() {
        val item = ParsedItem()
        assertEquals("", item.name)
        assertEquals(1, item.qty)
        assertNull(item.unit)
        assertNull(item.notes)
        assertNull(item.expiresAt)
    }

    @Test
    fun `ParseResult should initialize with empty list`() {
        val result = ParseResult()
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `SearchAnswer should initialize empty`() {
        val answer = SearchAnswer()
        assertEquals("", answer.answer)
        assertTrue(answer.citedIds.isEmpty())
    }
}
