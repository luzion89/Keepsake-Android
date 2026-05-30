package com.keepsake.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "keepsake_settings")

class KeepsakeDataStore(private val context: Context) {

    companion object Keys {
        val LANG = stringPreferencesKey("lang")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AI_ENABLED = booleanPreferencesKey("ai_enabled")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_DEEPSEEK_KEY = stringPreferencesKey("ai_deepseek_key")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val DEVICE_ID = stringPreferencesKey("device_id")
    }

    val language: Flow<String> = context.dataStore.data.map { it[LANG] ?: "zh" }
    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "system" }
    val aiEnabled: Flow<Boolean> = context.dataStore.data.map { it[AI_ENABLED] ?: false }
    val aiProvider: Flow<String> = context.dataStore.data.map { it[AI_PROVIDER] ?: "deepseek" }
    val aiApiKey: Flow<String> = context.dataStore.data.map { it[AI_API_KEY] ?: "" }
    val aiDeepseekKey: Flow<String> = context.dataStore.data.map { it[AI_DEEPSEEK_KEY] ?: "" }
    val aiModel: Flow<String> = context.dataStore.data.map { it[AI_MODEL] ?: "" }
    val deviceId: Flow<String> = context.dataStore.data.map { it[DEVICE_ID] ?: "" }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANG] = lang }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setAiEnabled(enabled: Boolean) {
        context.dataStore.edit { it[AI_ENABLED] = enabled }
    }

    suspend fun setAiProvider(provider: String) {
        context.dataStore.edit { it[AI_PROVIDER] = provider }
    }

    suspend fun setAiApiKey(key: String) {
        context.dataStore.edit { it[AI_API_KEY] = key }
    }

    suspend fun setAiDeepseekKey(key: String) {
        context.dataStore.edit { it[AI_DEEPSEEK_KEY] = key }
    }

    suspend fun setAiModel(model: String) {
        context.dataStore.edit { it[AI_MODEL] = model }
    }

    suspend fun setDeviceId(id: String) {
        context.dataStore.edit { it[DEVICE_ID] = id }
    }
}
