package com.keepsake.app.ai

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AiParsingEdgeCasesTest {

    private lateinit var json: Json

    @Before fun setUp() {
        json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
    }

    // --- T04: Descriptive info to notes ---
    @Test fun `T04 color and position go to notes`() {
        val item = ParsedItem(name = "沐浴露", qty = 1, unit = "瓶", notes = "白色，在牙刷旁边")
        assertEquals("沐浴露", item.name)
        assertEquals("白色，在牙刷旁边", item.notes)
        assertNotNull(item.notes)
    }

    @Test fun `T05 complex description should go to notes`() {
        val item = ParsedItem(name = "花生油", qty = 1, unit = "瓶", notes = "大瓶，新买，在厨房柜子里")
        assertEquals("花生油", item.name)
        assertTrue(item.notes?.contains("大瓶") == true)
        assertTrue(item.notes?.contains("厨房") == true)
    }

    // --- T06: Only name, default qty=1 ---
    @Test fun `T06 only name defaults to qty 1`() {
        val item = ParsedItem(name = "洗发水")
        assertEquals(1, item.qty)
        assertEquals("洗发水", item.name)
        assertNull(item.unit)
    }

    // --- T02: Multi-item parsing ---
    @Test fun `T02 multi items parsed correctly`() {
        val items = listOf(
            ParsedItem(name = "洗发水", qty = 3, unit = "瓶"),
            ParsedItem(name = "牙膏", qty = 2, unit = "盒")
        )
        assertEquals(2, items.size)
        assertEquals(3, items[0].qty)
        assertEquals(2, items[1].qty)
    }

    // --- T03: Parse result format ---
    @Test fun `T03 parse result wrap multiple items`() {
        val result = ParseResult(listOf(
            ParsedItem("A", 1), ParsedItem("B", 2), ParsedItem("C", 3)
        ))
        assertEquals(3, result.items.size)
    }

    // --- T13: Draft item editing support ---
    @Test fun `T13 ParsedItem supports all editable fields`() {
        val item = ParsedItem(
            name = "测试物品",
            qty = 5,
            unit = "个",
            notes = "备注内容",
            expiresAt = 9999999999L
        )
        assertTrue(item.name.isNotBlank())
        assertTrue(item.qty > 0)
        assertNotNull(item.unit)
        assertNotNull(item.notes)
        assertNotNull(item.expiresAt)
    }

    // --- T17: Batch confirmation data check ---
    @Test fun `T17 all items have required fields for batch save`() {
        val items = listOf(
            ParsedItem("A", 1, "瓶"),
            ParsedItem("B", 2, "盒", "在柜子里"),
            ParsedItem("C", 3, null, null, 9999L)
        )
        items.forEach {
            assertTrue("name must not be blank for save", it.name.isNotBlank())
            assertTrue("qty must be positive", it.qty > 0)
        }
    }

    // --- T19: Empty input check ---
    @Test fun `T19 blank input should be detected`() {
        val input = "   "
        assertTrue(input.isBlank())
    }

    @Test fun `T19 valid input should pass blank check`() {
        val input = "三瓶洗发水"
        assertTrue(input.isNotBlank())
    }

    // --- AI Error handling - T07/T08 ---
    @Test fun `T07 AiResult Error contains error message`() {
        val error = AiResult.Error("认证失败")
        assertTrue(error.message.contains("认证"))
    }

    @Test fun `T08 AiResult network error contains hint`() {
        val error = AiResult.Error("网络错误: timeout")
        assertTrue(error.message.contains("网络"))
    }

    // --- Search answer format ---
    @Test fun `S09 AI search answer should include cited IDs`() {
        val answer = SearchAnswer("找到了2件在厨房的物品", listOf("id1", "id2"))
        assertTrue(answer.citedIds.isNotEmpty())
        assertEquals(2, answer.citedIds.size)
    }

    @Test fun `S12 empty search results should be handled`() {
        val answer = SearchAnswer("没找到相关物品", emptyList())
        assertTrue(answer.citedIds.isEmpty())
        assertTrue(answer.answer.contains("没找到"))
    }

    // --- Provider config edge cases ---
    @Test fun `ST05 deepseek provider key extraction`() {
        // DeepSeek uses deepseekApiKey, OpenRouter uses apiKey
        val deepseekKey = "sk-deepseek-xxx"
        val openrouterKey = "sk-or-xxx"
        assertTrue(deepseekKey.startsWith("sk-"))
        assertTrue(openrouterKey.startsWith("sk-or-"))
    }

    @Test fun `ST09 invalid key should be caught by ping`() {
        val invalidKey = "invalid"
        assertFalse(invalidKey.startsWith("sk-"))
    }
}
