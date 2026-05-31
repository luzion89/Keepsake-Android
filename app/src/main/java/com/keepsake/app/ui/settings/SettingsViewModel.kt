package com.keepsake.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepsake.app.ai.AiService
import com.keepsake.app.data.local.datastore.KeepsakeDataStore
import com.keepsake.app.data.repository.AreaRepository
import com.keepsake.app.data.repository.ItemRepository
import com.keepsake.app.data.repository.PhotoRepository
import com.keepsake.app.data.repository.RoomRepository
import com.keepsake.app.domain.model.AiConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val language: String = "zh",
    val theme: String = "system",
    val aiConfig: AiConfig = AiConfig(),
    val deviceId: String = "",
    val roomCount: Int = 0,
    val areaCount: Int = 0,
    val itemCount: Int = 0,
    val photoCount: Int = 0,
    val isTestingApi: Boolean = false,
    val apiTestResult: String? = null,
    val showExportSuccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: KeepsakeDataStore,
    private val roomRepo: RoomRepository,
    private val areaRepo: AreaRepository,
    private val itemRepo: ItemRepository,
    private val photoRepo: PhotoRepository,
    private val aiService: AiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { dataStore.language.collect { v -> _uiState.update { it.copy(language = v) } } }
        viewModelScope.launch { dataStore.themeMode.collect { v -> _uiState.update { it.copy(theme = v) } } }
        viewModelScope.launch { dataStore.deviceId.collect { v -> _uiState.update { it.copy(deviceId = v) } } }
        viewModelScope.launch { dataStore.aiEnabled.collect { v -> _uiState.update { it.copy(aiConfig = it.aiConfig.copy(enabled = v)) } } }
        viewModelScope.launch { dataStore.aiProvider.collect { v -> _uiState.update { it.copy(aiConfig = it.aiConfig.copy(provider = v)) } } }
        viewModelScope.launch { dataStore.aiApiKey.collect { v -> _uiState.update { it.copy(aiConfig = it.aiConfig.copy(apiKey = v)) } } }
        viewModelScope.launch { dataStore.aiDeepseekKey.collect { v -> _uiState.update { it.copy(aiConfig = it.aiConfig.copy(deepseekApiKey = v)) } } }
        viewModelScope.launch { dataStore.aiModel.collect { v -> _uiState.update { it.copy(aiConfig = it.aiConfig.copy(model = v)) } } }
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val rooms = roomRepo.count()
            val areas = areaRepo.count()
            val items = itemRepo.count()
            val photos = photoRepo.count()
            _uiState.update { it.copy(roomCount = rooms, areaCount = areas, itemCount = items, photoCount = photos) }
        }
    }

    fun setLanguage(lang: String) { viewModelScope.launch { dataStore.setLanguage(lang) } }
    fun setTheme(mode: String) { viewModelScope.launch { dataStore.setThemeMode(mode) } }

    fun setAiEnabled(enabled: Boolean) {
        _uiState.update { it.copy(aiConfig = it.aiConfig.copy(enabled = enabled)) }
        viewModelScope.launch { dataStore.setAiEnabled(enabled) }
    }

    fun setAiProvider(provider: String) {
        _uiState.update { it.copy(aiConfig = it.aiConfig.copy(provider = provider)) }
        viewModelScope.launch { dataStore.setAiProvider(provider) }
    }

    fun setAiApiKey(key: String) {
        _uiState.update { it.copy(aiConfig = it.aiConfig.copy(apiKey = key)) }
        viewModelScope.launch { dataStore.setAiApiKey(key) }
    }

    fun setAiDeepseekKey(key: String) {
        _uiState.update { it.copy(aiConfig = it.aiConfig.copy(deepseekApiKey = key)) }
        viewModelScope.launch { dataStore.setAiDeepseekKey(key) }
    }

    fun setAiModel(model: String) {
        _uiState.update { it.copy(aiConfig = it.aiConfig.copy(model = model)) }
        viewModelScope.launch { dataStore.setAiModel(model) }
    }

    fun testApiConnection(provider: String, apiKey: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingApi = true, apiTestResult = null) }
            when (val result = aiService.pingProvider(provider, apiKey)) {
                is com.keepsake.app.ai.AiResult.Success -> {
                    _uiState.update { it.copy(isTestingApi = false, apiTestResult = result.data) }
                }
                is com.keepsake.app.ai.AiResult.Error -> {
                    _uiState.update { it.copy(isTestingApi = false, apiTestResult = result.message) }
                }
            }
        }
    }

    fun generateDeviceId() {
        viewModelScope.launch {
            val id = java.util.UUID.randomUUID().toString().take(8)
            dataStore.setDeviceId(id)
        }
    }

    fun clearApiTestResult() {
        _uiState.update { it.copy(apiTestResult = null) }
    }
}
