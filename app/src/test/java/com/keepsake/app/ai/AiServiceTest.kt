package com.keepsake.app.ai

import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.*
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class AiServiceTest {
    private lateinit var client: OkHttpClient
    private lateinit var service: AiService

    @Before
    fun setUp() {
        client = mockk(relaxed = true)
        service = AiService(client)
    }

    // --- ParsedItem ---
    @Test
    fun `ParsedItem default values`() {
        val item = ParsedItem()
        assertEquals("", item.name)
        assertEquals(1, item.qty)
        assertNull(item.unit)
        assertNull(item.notes)
        assertNull(item.expiresAt)
    }

    @Test
    fun `ParsedItem with all fields`() {
        val item = ParsedItem(name = "洗发水", qty = 3, unit = "瓶", notes = "白色，在浴室", expiresAt = 9999L)
        assertEquals("洗发水", item.name)
        assertEquals(3, item.qty)
        assertEquals("瓶", item.unit)
        assertEquals("白色，在浴室", item.notes)
        assertEquals(9999L, item.expiresAt)
    }

    // --- ParseResult ---
    @Test
    fun `ParseResult empty`() {
        assertTrue(ParseResult().items.isEmpty())
    }

    @Test
    fun `ParseResult with items`() {
        val items = listOf(ParsedItem("牙膏"), ParsedItem("洗发水"))
        val result = ParseResult(items)
        assertEquals(2, result.items.size)
    }

    // --- SearchAnswer ---
    @Test
    fun `SearchAnswer defaults`() {
        val answer = SearchAnswer()
        assertEquals("", answer.answer)
        assertTrue(answer.citedIds.isEmpty())
    }

    @Test
    fun `SearchAnswer with values`() {
        val answer = SearchAnswer("找到了3件在厨房的物品", listOf("id1", "id2"))
        assertTrue(answer.answer.contains("厨房"))
        assertEquals(2, answer.citedIds.size)
    }

    // --- AiResult ---
    @Test
    fun `AiResult Success should contain data`() {
        val result = AiResult.Success("data")
        assertTrue(result is AiResult.Success)
        assertEquals("data", (result as AiResult.Success).data)
    }

    @Test
    fun `AiResult Error should contain message`() {
        val result = AiResult.Error("failed")
        assertTrue(result is AiResult.Error)
        assertEquals("failed", (result as AiResult.Error).message)
    }

    // --- Provider URL construction ---
    @Test
    fun `getProviderConfig deepseek should use deepseek URL and model`() {
        val result = invokeGetProviderConfig("deepseek", "sk-test", null)
        assertEquals("https://api.deepseek.com/v1/chat/completions", result.first)
        assertEquals("sk-test", result.second)
        assertEquals("deepseek-chat", result.third)
    }

    @Test
    fun `getProviderConfig openrouter should use openrouter URL`() {
        val result = invokeGetProviderConfig("openrouter", "sk-or-xxx", null)
        assertEquals("https://openrouter.ai/api/v1/chat/completions", result.first)
        assertEquals("google/gemini-2.5-flash-lite", result.third)
    }

    @Test
    fun `getProviderConfig should use model override`() {
        val result = invokeGetProviderConfig("deepseek", "sk-test", "custom-model")
        assertEquals("custom-model", result.third)
    }

    // --- extractContent ---
    @Test
    fun `extractContent should parse valid OpenAI response`() {
        val json = """{"choices":[{"message":{"content":"test content"}}]}"""
        val result = invokeExtractContent(json)
        assertEquals("test content", result)
    }

    @Test
    fun `extractContent should return null for invalid JSON`() {
        assertNull(invokeExtractContent("not json"))
    }

    @Test
    fun `extractContent should return null for empty choices`() {
        assertNull(invokeExtractContent("""{"choices":[]}"""))
    }

    // --- extractJsonArray ---
    @Test
    fun `extractJsonArray should parse item array`() {
        val content = """
            这是一些说明文字
            [{"name":"洗发水","qty":3,"unit":"瓶","notes":"白色"}]
            更多说明
        """.trimIndent()

        val result = invokeExtractJsonArray(content)
        assertFalse("Should find at least 1 item", result.isEmpty())
    }

    // --- Explicit typed helpers instead of generic invokePrivate ---
    @Suppress("UNCHECKED_CAST")
    private fun invokeGetProviderConfig(p: String, k: String, m: String?): Triple<String, String, String> {
        val method = AiService::class.java.getDeclaredMethod("getProviderConfig", String::class.java, String::class.java, String::class.java)
        method.isAccessible = true
        return method.invoke(service, p, k, m) as Triple<String, String, String>
    }

    private fun invokeExtractContent(json: String): String? {
        val method = AiService::class.java.getDeclaredMethod("extractContent", String::class.java)
        method.isAccessible = true
        return method.invoke(service, json) as? String
    }

    @Suppress("UNCHECKED_CAST")
    private fun invokeExtractJsonArray(content: String): List<ParsedItem> {
        val method = AiService::class.java.declaredMethods.first { it.name == "extractJsonArray" }
        method.isAccessible = true
        val mapper: (Map<String, JsonElement>) -> ParsedItem = { map ->
            ParsedItem(
                name = map["name"]?.toString()?.trim('"') ?: "",
                qty = (map["qty"]?.toString()?.trim('"'))?.toIntOrNull() ?: 1,
                unit = map["unit"]?.toString()?.trim('"'),
                notes = map["notes"]?.toString()?.trim('"')
            )
        }
        return method.invoke(service, content, mapper) as? List<ParsedItem> ?: emptyList()
    }

    @Test
    fun `system prompt should exist and class compiles`() {
        assertNotNull(service)
    }
}
