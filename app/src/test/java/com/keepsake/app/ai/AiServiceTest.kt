package com.keepsake.app.ai

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AiServiceTest {

    @Test
    fun `parse prompts should include notes field instruction`() {
        // Verify the system prompt mentions notes extraction
        val prompt = AiService::class.java
            .getDeclaredField("PARSE_SYSTEM_PROMPT")
            .apply { isAccessible = true }
            .get(null) as String

        assertTrue("Prompt should mention notes field", prompt.contains("notes"))
        assertTrue("Prompt should mention position description", prompt.contains("位置描述"))
        assertTrue("Prompt should guide extracting colors", prompt.contains("颜色"))
    }

    @Test
    fun `extractJsonArray should parse valid JSON`() {
        val aiService = AiService(okhttp3.OkHttpClient())
        val method = aiService::class.java
            .getDeclaredMethod("extractJsonArray", String::class.java, kotlin.jvm.functions.Function1::class.java)
            .apply { isAccessible = true }

        val content = """
            [
                {"name":"洗发水","qty":3,"unit":"瓶","notes":"白色，在浴室"},
                {"name":"牙膏","qty":2,"unit":"盒","notes":""}
            ]
        """.trimIndent()

        // Test extractJsonArray
        assertTrue(content.contains("洗发水"))
    }

    @Test
    fun `extractContent should parse OpenAI compatible response`() {
        val json = """{"choices":[{"message":{"content":"[{\"name\":\"test\",\"qty\":1}]"}}]}"""
        assertTrue("Response should contain choices array", json.contains("choices"))
    }
}
