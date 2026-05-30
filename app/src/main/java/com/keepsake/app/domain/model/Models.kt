package com.keepsake.app.domain.model

data class Room(
    val id: String = "",
    val name: String = "",
    val icon: String? = null,
    val photoIds: List<String> = emptyList(),
    val note: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String = "",
    val areaCount: Int = 0
)

data class Area(
    val id: String = "",
    val roomId: String = "",
    val name: String = "",
    val photoIds: List<String> = emptyList(),
    val note: String? = null,
    val updatedAt: Long = 0L,
    val itemCount: Int = 0
)

data class Photo(
    val id: String = "",
    val parentType: String = "",
    val parentId: String = "",
    val takenAt: Long = 0L,
    val blobUri: String? = null,
    val recognitionStatus: String = "pending"
)

data class ReminderRule(
    val id: String = "",
    val itemId: String = "",
    val kind: String = "",
    val thresholdAt: Long? = null,
    val thresholdQty: Int? = null,
    val note: String? = null,
    val lastFiredAt: Long? = null
)

data class AiConfig(
    val enabled: Boolean = false,
    val provider: String = "deepseek",
    val apiKey: String = "",
    val deepseekApiKey: String = "",
    val model: String = ""
)

data class Item(
    val id: String = "",
    val areaId: String = "",
    val name: String = "",
    val qty: Int = 0,
    val unit: String? = null,
    val tags: List<String> = emptyList(),
    val photoIds: List<String> = emptyList(),
    val expiresAt: Long? = null,
    val source: String = "manual",
    val confidence: Double? = null,
    val notes: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val updatedBy: String = "",
    val deleted: Boolean = false
)

enum class ItemSource(val value: String) {
    AI("ai"),
    VOICE("voice"),
    MANUAL("manual");

    companion object {
        fun from(value: String) = entries.find { it.value == value } ?: MANUAL
    }
}
