package com.keepsake.app.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepsake.app.data.repository.ItemRepository
import com.keepsake.app.data.repository.ReminderRepository
import com.keepsake.app.domain.model.Item
import com.keepsake.app.domain.model.ReminderRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemUiState(
    val item: Item? = null,
    val reminders: List<ReminderRule> = emptyList(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editQty: String = "",
    val editUnit: String = "",
    val editNotes: String = "",
    val editTags: String = "",
    val editExpiresAt: Long? = null,
    val deleteDialogVisible: Boolean = false
)

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemRepo: ItemRepository,
    private val reminderRepo: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemUiState())
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    fun load(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val item = itemRepo.getById(itemId)
            item?.let { populateEditFields(it) }
            _uiState.update { it.copy(item = item, isLoading = false) }
            val rules = reminderRepo.getByItem(itemId)
            _uiState.update { it.copy(reminders = rules) }
        }
    }

    private fun populateEditFields(item: Item) {
        _uiState.update {
            it.copy(
                editName = item.name,
                editQty = item.qty.toString(),
                editUnit = item.unit ?: "",
                editNotes = item.notes ?: "",
                editTags = item.tags.joinToString(", "),
                editExpiresAt = item.expiresAt
            )
        }
    }

    fun toggleEdit() {
        val editing = !_uiState.value.isEditing
        if (!editing) {
            // Cancel: restore
            val item = _uiState.value.item ?: return
            populateEditFields(item)
        }
        _uiState.update { it.copy(isEditing = editing) }
    }

    fun saveEdit() {
        val item = _uiState.value.item ?: return
        val state = _uiState.value
        viewModelScope.launch {
            itemRepo.update(
                item.copy(
                    name = state.editName.trim(),
                    qty = state.editQty.toIntOrNull() ?: item.qty,
                    unit = state.editUnit.ifBlank { null },
                    notes = state.editNotes.ifBlank { null },
                    tags = state.editTags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    expiresAt = state.editExpiresAt
                )
            )
            val updated = itemRepo.getById(item.id)
            _uiState.update { it.copy(item = updated, isEditing = false) }
        }
    }

    fun qtyDelta(delta: Int) {
        val item = _uiState.value.item ?: return
        viewModelScope.launch { itemRepo.qtyDelta(item.id, delta) }
    }

    fun deleteItem() {
        val item = _uiState.value.item ?: return
        viewModelScope.launch { itemRepo.softDelete(item.id) }
    }

    fun showDeleteDialog() = _uiState.update { it.copy(deleteDialogVisible = true) }
    fun dismissDeleteDialog() = _uiState.update { it.copy(deleteDialogVisible = false) }

    fun updateEditName(v: String) = _uiState.update { it.copy(editName = v.take(120)) }
    fun updateEditQty(v: String) = _uiState.update { it.copy(editQty = v.filter { c -> c.isDigit() }) }
    fun updateEditUnit(v: String) = _uiState.update { it.copy(editUnit = v.take(20)) }
    fun updateEditNotes(v: String) = _uiState.update { it.copy(editNotes = v.take(4000)) }
    fun updateEditTags(v: String) = _uiState.update { it.copy(editTags = v) }
    fun updateEditExpiresAt(v: Long?) = _uiState.update { it.copy(editExpiresAt = v) }

    fun addReminder(kind: String, thresholdAt: Long? = null, thresholdQty: Int? = null) {
        val item = _uiState.value.item ?: return
        viewModelScope.launch {
            reminderRepo.create(item.id, kind, thresholdAt, thresholdQty)
            _uiState.update { it.copy(reminders = reminderRepo.getByItem(item.id)) }
        }
    }

    fun removeReminder(ruleId: String) {
        viewModelScope.launch {
            reminderRepo.softDelete(ruleId)
            val item = _uiState.value.item ?: return@launch
            _uiState.update { it.copy(reminders = reminderRepo.getByItem(item.id)) }
        }
    }
}
