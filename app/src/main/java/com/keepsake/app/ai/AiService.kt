package com.keepsake.app.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import kotlinx.serialization.builtins.serializer

@Serializable
data class ParsedItem(
    val name: String = "",
    val qty: Int = 1,
    val unit: String? = null,
    val notes: String? = null,
    val expiresAt: Long? = null
)

@Serializable
data class ParseResult(val items: List<ParsedItem> = emptyList())

@Serializable
data class SearchAnswer(val answer: String = "", val citedIds: List<String> = emptyList())

sealed class AiResult<out T> {
    data class Success<T>(val data: T) : AiResult<T>()
    data class Error(val message: String) : AiResult<Nothing>()
}

class AiService(private val client: OkHttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

        private const val DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions"
        private const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
        private const val DEEPSEEK_DEFAULT_MODEL = "deepseek-chat"
        private const val OPENROUTER_DEFAULT_MODEL = "google/gemini-2.5-flash-lite"

        private val PARSE_SYSTEM_PROMPT = """
你是一个家庭物品管理助手。你的任务是将用户的自由文本描述解析为结构化的物品清单。

规则：
1. 提取物品名称到 name 字段（保持简洁，去掉修饰词）
2. 提取数量到 qty（数字，默认为1）
3. 提取单位到 unit（如 瓶、盒、袋、个、包等）
4. **将修饰性、描述性、位置性信息放入 notes 字段**。包括但不限于：
   - 颜色（红色、蓝色等）
   - 品牌（飘柔、云南白药等）
   - 位置描述（在柜子里、在牙刷旁边、放在沙发上等）
   - 新旧程度（新的、旧的、用了一半等）
   - 容量规格（大瓶、小瓶、500ml等）
5. 提取过期日期到 expires_at（如果有的话，返回时间戳）
6. 仅返回 JSON 数组，格式：[{"name":"物品名","qty":数量,"unit":"单位","notes":"描述","expires_at":时间戳}]

示例：
输入："白色大瓶沐浴露在牙刷旁边"
输出：[{"name":"沐浴露","qty":1,"unit":"瓶","notes":"白色，大瓶，在牙刷旁边"}]

输入："厨房柜子里新买的飘柔洗发水两瓶，门边角落放了半瓶快过期的洗洁精"
输出：[{"name":"洗发水","qty":2,"unit":"瓶","notes":"新买的，飘柔品牌，在厨房柜子里"},{"name":"洗洁精","qty":1,"unit":"瓶","notes":"半瓶，快过期，在门边角落"}]

输入："三瓶可乐"
输出：[{"name":"可乐","qty":3,"unit":"瓶"}]
        """.trimIndent()

        private val SEARCH_SYSTEM_PROMPT = """
你是一个家庭物品管理助手。根据用户当前拥有的物品清单回答问题。

当前物品清单：
{context}

规则：
1. 根据物品清单回答用户的问题
2. 如果物品清单中有相关物品，在 answer 中引用它们
3. citedIds 列出引用的物品 ID
4. 返回 JSON：{"answer":"回答内容","citedIds":["id1","id2"]}
        """.trimIndent()
    }

    suspend fun parseItemsFromText(
        text: String,
        existingItemsJson: String? = null,
        provider: String = "deepseek",
        apiKey: String = "",
        modelOverride: String? = null
    ): AiResult<List<ParsedItem>> {
        val (url, apiKeyUsed, model) = getProviderConfig(provider, apiKey, modelOverride)

        val userMessage = if (existingItemsJson != null) {
            "当前物品清单：$existingItemsJson\n\n用户描述：$text\n\n请将新物品合并到现有清单中。"
        } else {
            text
        }

        val messages = listOf(
            mapOf("role" to "system", "content" to PARSE_SYSTEM_PROMPT),
            mapOf("role" to "user", "content" to userMessage)
        )

        return when (val result = callApiRaw(url, apiKeyUsed, model, messages)) {
            is AiResult.Success -> {
                val content = extractContent(result.data) ?: return AiResult.Success(emptyList())
                AiResult.Success(extractJsonArray(content) { obj ->
                    ParsedItem(
                        name = obj["name"]?.toString()?.trim('"') ?: "",
                        qty = (obj["qty"]?.toString()?.trim('"'))?.toIntOrNull() ?: 1,
                        unit = obj["unit"]?.toString()?.trim('"'),
                        notes = obj["notes"]?.toString()?.trim('"'),
                        expiresAt = (obj["expires_at"]?.toString()?.trim('"'))?.toLongOrNull()
                    )
                })
            }
            is AiResult.Error -> result
        }
    }

    suspend fun searchAnswer(
        query: String,
        contextItems: List<Map<String, String>>,
        provider: String = "deepseek",
        apiKey: String = ""
    ): AiResult<SearchAnswer> {
        val (url, apiKeyUsed, model) = getProviderConfig(provider, apiKey)

        val contextJson = contextItems.joinToString(",\n") { item ->
            """{"id":"${item["id"]}","name":"${item["name"]}","qty":${item["qty"]},"location":"${item["location"]}","notes":"${item["notes"]}"}"""
        }
        val systemPrompt = SEARCH_SYSTEM_PROMPT.replace("{context}", "[$contextJson]")

        val messages = listOf(
            mapOf("role" to "system", "content" to systemPrompt),
            mapOf("role" to "user", "content" to query)
        )

        return when (val result = callApiRaw(url, apiKeyUsed, model, messages)) {
            is AiResult.Success -> {
                val content = extractContent(result.data) ?: return AiResult.Success(SearchAnswer("", emptyList()))
                val parsed = json.decodeFromString<SearchAnswer>(content)
                AiResult.Success(parsed)
            }
            is AiResult.Error -> result
        }
    }

    suspend fun pingProvider(provider: String, apiKey: String): AiResult<String> {
        val (url, apiKeyUsed, _) = getProviderConfig(provider, apiKey)
        val requestBody = """{"model":"gpt-3.5-turbo","messages":[{"role":"user","content":"ping"}],"max_tokens":1}"""
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKeyUsed")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody(JSON_MEDIA))
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) AiResult.Success("连接成功")
                else AiResult.Error("认证失败 (${response.code})")
            } catch (e: Exception) {
                AiResult.Error("连接失败: ${e.message}")
            }
        }
    }

    private fun getProviderConfig(
        provider: String,
        apiKey: String,
        modelOverride: String? = null
    ): Triple<String, String, String> {
        return if (provider == "openrouter") {
            Triple(
                OPENROUTER_URL,
                apiKey,
                modelOverride ?: OPENROUTER_DEFAULT_MODEL
            )
        } else {
            Triple(
                DEEPSEEK_URL,
                apiKey,
                modelOverride ?: DEEPSEEK_DEFAULT_MODEL
            )
        }
    }

    private suspend fun callApiRaw(
        url: String,
        apiKey: String,
        model: String,
        messages: List<Map<String, String>>
    ): AiResult<String> {
        val msgsJson = messages.joinToString(",") { msg ->
            val role = msg["role"] ?: "user"
            val content = (msg["content"] ?: "").replace("\"", "\\\"").replace("\n", "\\n")
            """{"role":"$role","content":"$content"}"""
        }
        val bodyStr = """{"model":"$model","messages":[$msgsJson],"temperature":0.1}"""

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(bodyStr.toRequestBody(JSON_MEDIA))
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    AiResult.Error("请求失败 (${response.code}): $body")
                } else {
                    AiResult.Success(body)
                }
            } catch (e: Exception) {
                AiResult.Error("网络错误: ${e.message}")
            }
        }
    }

    private fun extractContent(responseBody: String): String? {
        return try {
            val jsonObj = kotlinx.serialization.json.Json.parseToJsonElement(responseBody)
            val choices = jsonObj.jsonObject["choices"]?.jsonArray ?: return null
            val first = choices.firstOrNull()?.jsonObject ?: return null
            first["message"]?.jsonObject?.get("content")?.toString()?.trim('"')
        } catch (e: Exception) {
            null
        }
    }

    private fun <T> extractJsonArray(content: String, mapper: (Map<String, JsonElement>) -> T): List<T> {
        return try {
            val cleaned = content
                .substringAfter("[")
                .substringBeforeLast("]")
                .let { "[$it]" }
            val arr = json.parseToJsonElement(cleaned).jsonArray
            arr.map { elem ->
                val map = elem.jsonObject.entries.associate { e -> e.key to e.value }
                mapper(map)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
