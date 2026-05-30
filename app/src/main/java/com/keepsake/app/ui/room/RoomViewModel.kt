package com.keepsake.app.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepsake.app.data.repository.AreaRepository
import com.keepsake.app.data.repository.RoomRepository
import com.keepsake.app.domain.model.Area
import com.keepsake.app.domain.model.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomUiState(
    val room: Room? = null,
    val areas: List<Area> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val editingAreaId: String? = null,
    val editingName: String = ""
)

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepo: RoomRepository,
    private val areaRepo: AreaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    fun load(roomId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val room = roomRepo.getById(roomId)
            _uiState.update { it.copy(room = room) }
            areaRepo.observeByRoom(roomId).collect { areas ->
                _uiState.update { it.copy(areas = areas, isLoading = false) }
            }
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun dismissAddDialog() = _uiState.update { it.copy(showAddDialog = false) }

    fun createArea(name: String) {
        if (name.isBlank()) return
        val roomId = _uiState.value.room?.id ?: return
        viewModelScope.launch {
            areaRepo.create(roomId, name)
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    fun startEditing(area: Area) {
        _uiState.update { it.copy(editingAreaId = area.id, editingName = area.name) }
    }

    fun updateEditingName(name: String) {
        _uiState.update { it.copy(editingName = name) }
    }

    fun commitRename() {
        val state = _uiState.value
        val id = state.editingAreaId ?: return
        val name = state.editingName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            areaRepo.rename(id, name)
            _uiState.update { it.copy(editingAreaId = null, editingName = "") }
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(editingAreaId = null, editingName = "") }
    }

    fun deleteArea(id: String) {
        viewModelScope.launch { areaRepo.softDelete(id) }
    }
}
