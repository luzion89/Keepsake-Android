package com.keepsake.app.ui.textinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepsake.app.ai.AiService
import com.keepsake.app.ai.ParsedItem
import com.keepsake.app.data.local.datastore.KeepsakeDataStore
import com.keepsake.app.data.repository.AreaRepository
import com.keepsake.app.data.repository.ItemRepository
import com.keepsake.app.domain.model.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TextInputUiState(
    val areaName: String = "",
    val text: String = "",
    val draftItems: List<ParsedItem> = emptyList(),
    val existingItems: List<Item> = emptyList(),
    val isParsing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val mode: String = "merge" // merge / replace
)

data class ParsedItemWithId(
    val tempId: String,
    val item: ParsedItem,
    val isNew: Boolean
)

@HiltViewModel
class TextInputViewModel @Inject constructor(
    private val areaRepo: AreaRepository,
    private val itemRepo: ItemRepository,
    private val aiService: AiService,
    private val dataStore: KeepsakeDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextInputUiState())
    val uiState: StateFlow<TextInputUiState> = _uiState.asStateFlow()

    private val _draftItems = MutableStateFlow<List<ParsedItemWithId>>(emptyList())
    val draftItems: StateFlow<List<ParsedItemWithId>> = _draftItems.asStateFlow()

    private var currentAreaId: String = ""

    fun load(areaId: String) {
        currentAreaId = areaId
        viewModelScope.launch {
            val area = areaRepo.getById(areaId)
            _uiState.update { it.copy(areaName = area?.name ?: "") }
            itemRepo.observeByArea(areaId).collect { items ->
                _uiState.update { it.copy(existingItems = items) }
            }
        }
    }

    fun updateText(text: String) = _uiState.update { it.copy(text = text, error = null) }

    fun toggleMode() {
        val newMode = if (_uiState.value.mode == "merge") "replace" else "merge"
        _uiState.update { it.copy(mode = newMode) }
    }

    fun parse() {
        val text = _uiState.value.text.trim()
        if (text.isBlank()) {
            _uiState.update { it.copy(error = "请输入内容") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isParsing = true, error = null) }
            val provider = dataStore.aiProvider.first()
            val apiKey = if (provider == "deepseek") dataStore.aiDeepseekKey.first() else dataStore.aiApiKey.first()
            val model = dataStore.aiModel.first().ifBlank { null }
            val mode = _uiState.value.mode

            val existingJson = if (mode == "merge" && _uiState.value.existingItems.isNotEmpty()) {
                _uiState.value.existingItems.joinToString(",\n") {
                    """{"id":"${it.id}","name":"${it.name}","qty":${it.qty},"unit":"${it.unit ?: "" }","notes":"${it.notes ?: ""}"}"""
                }.let { "[$it]" }
            } else null

            when (val result = aiService.parseItemsFromText(text, existingJson, provider, apiKey, model)) {
                is com.keepsake.app.ai.AiResult.Success -> {
                    val items = result.data.mapIndexed { i, item ->
                        val existingItem = _uiState.value.existingItems.find {
                            it.name.equals(item.name, ignoreCase = true)
                        }
                        ParsedItemWithId(
                            tempId = "new_$i",
                            item = item,
                            isNew = existingItem == null
                        )
                    }
                    _draftItems.value = items
                    _uiState.update { it.copy(isParsing = false) }
                }
                is com.keepsake.app.ai.AiResult.Error -> {
                    _uiState.update { it.copy(isParsing = false, error = result.message) }
                }
            }
        }
    }

    fun updateDraftItem(tempId: String, updated: ParsedItem) {
        _draftItems.update { list ->
            list.map { if (it.tempId == tempId) it.copy(item = updated) else it }
        }
    }

    fun removeDraftItem(tempId: String) {
        _draftItems.update { it.filter { d -> d.tempId != tempId } }
    }

    fun addManualDraft() {
        val newId = "manual_${System.currentTimeMillis()}"
        _draftItems.update {
            it + ParsedItemWithId(tempId = newId, item = ParsedItem(name = "", qty = 1), isNew = true)
        }
    }

    fun confirmSave() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                if (_uiState.value.mode == "replace") {
                    // Delete all existing items in area
                    for (item in _uiState.value.existingItems) {
                        itemRepo.softDelete(item.id)
                    }
                }

                for (draft in _draftItems.value) {
                    val item = draft.item
                    if (item.name.isBlank()) continue

                    val existingItem = _uiState.value.existingItems.find {
                        it.name.equals(item.name, ignoreCase = true)
                    }
                    if (existingItem != null) {
                        // Merge: update quantity
                        itemRepo.qtyDelta(existingItem.id, item.qty)
                    } else {
                        // Create new
                        itemRepo.create(
                            areaId = currentAreaId,
                            name = item.name,
                            qty = item.qty,
                            unit = item.unit,
                            notes = item.notes,
                            source = "ai"
                        )
                    }
                }
                _uiState.update { it.copy(isSaving = false, text = "") }
                _draftItems.value = emptyList()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "保存失败: ${e.message}") }
            }
        }
    }
}
