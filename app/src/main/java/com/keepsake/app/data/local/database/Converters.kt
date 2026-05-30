package com.keepsake.app.data.local.database

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromStringList(value: String?): String {
        return value ?: "[]"
    }

    @TypeConverter
    fun toStringList(value: String): String {
        return value.ifBlank { "[]" }
    }

    @TypeConverter
    fun fromLongList(value: Long?): Long {
        return value ?: 0L
    }

    @TypeConverter
    fun toLong(value: Long): Long {
        return value
    }
}
