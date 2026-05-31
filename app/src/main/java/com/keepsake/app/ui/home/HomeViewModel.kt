package com.keepsake.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepsake.app.data.repository.RoomRepository
import com.keepsake.app.domain.model.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val rooms: List<Room> = emptyList(),
    val isLoading: Boolean = true,
    val showBottomSheet: Boolean = false,
    val editingRoomId: String? = null,
    val editingName: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val roomRepo: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            roomRepo.observeAll().collect { rooms ->
                _uiState.update { it.copy(rooms = rooms, isLoading = false) }
            }
        }
    }

    fun showBottomSheet() = _uiState.update { it.copy(showBottomSheet = true) }

    fun dismissBottomSheet() = _uiState.update { it.copy(showBottomSheet = false) }

    fun createRoom(name: String, icon: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            roomRepo.create(name, icon)
            _uiState.update { it.copy(showBottomSheet = false) }
        }
    }

    fun startEditing(room: Room) {
        _uiState.update { it.copy(editingRoomId = room.id, editingName = room.name) }
    }

    fun updateEditingName(name: String) {
        _uiState.update { it.copy(editingName = name) }
    }

    fun commitRename() {
        val state = _uiState.value
        val id = state.editingRoomId ?: return
        val name = state.editingName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            roomRepo.rename(id, name)
            _uiState.update { it.copy(editingRoomId = null, editingName = "") }
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(editingRoomId = null, editingName = "") }
    }

    fun deleteRoom(id: String) {
        viewModelScope.launch {
            roomRepo.softDelete(id)
        }
    }
}
