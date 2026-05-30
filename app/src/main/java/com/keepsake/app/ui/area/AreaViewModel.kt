package com.keepsake.app.ui.area

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepsake.app.data.repository.AreaRepository
import com.keepsake.app.data.repository.ItemRepository
import com.keepsake.app.data.repository.PhotoRepository
import com.keepsake.app.domain.model.Area
import com.keepsake.app.domain.model.Item
import com.keepsake.app.domain.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AreaUiState(
    val area: Area? = null,
    val items: List<Item> = emptyList(),
    val photos: List<Photo> = emptyList(),
    val isLoading: Boolean = true,
    val showAddForm: Boolean = false,
    val addName: String = "",
    val addQty: String = "1",
    val editingItemId: String? = null,
    val editingName: String = "",
    val selectedPhotoIndex: Int = -1
)

@HiltViewModel
class AreaViewModel @Inject constructor(
    private val areaRepo: AreaRepository,
    private val itemRepo: ItemRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AreaUiState())
    val uiState: StateFlow<AreaUiState> = _uiState.asStateFlow()

    fun load(areaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val area = areaRepo.getById(areaId)
            _uiState.update { it.copy(area = area) }
            launch {
                itemRepo.observeByArea(areaId).collect { items ->
                    _uiState.update { it.copy(items = items) }
                }
            }
            launch {
                photoRepo.observeByParent("area", areaId).collect { photos ->
                    _uiState.update { it.copy(photos = photos) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleAddForm() = _uiState.update { it.copy(showAddForm = !it.showAddForm) }

    fun updateAddName(name: String) = _uiState.update { it.copy(addName = name) }

    fun updateAddQty(qty: String) = _uiState.update { it.copy(addQty = qty) }

    fun addItem() {
        val state = _uiState.value
        val name = state.addName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            itemRepo.create(
                areaId = state.area?.id ?: return@launch,
                name = name,
                qty = state.addQty.toIntOrNull() ?: 1
            )
            _uiState.update { it.copy(showAddForm = false, addName = "", addQty = "1") }
        }
    }

    fun qtyDelta(itemId: String, delta: Int) {
        viewModelScope.launch { itemRepo.qtyDelta(itemId, delta) }
    }

    fun startEditing(item: Item) {
        _uiState.update { it.copy(editingItemId = item.id, editingName = item.name) }
    }

    fun updateEditingName(name: String) {
        _uiState.update { it.copy(editingName = name) }
    }

    fun commitRename() {
        val state = _uiState.value
        val id = state.editingItemId ?: return
        val name = state.editingName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            itemRepo.renameInArea(id, name)
            _uiState.update { it.copy(editingItemId = null, editingName = "") }
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(editingItemId = null, editingName = "") }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch { itemRepo.softDelete(id) }
    }

    fun showPhotoAtIndex(index: Int) {
        _uiState.update { it.copy(selectedPhotoIndex = index) }
    }

    fun dismissPhoto() {
        _uiState.update { it.copy(selectedPhotoIndex = -1) }
    }
}
