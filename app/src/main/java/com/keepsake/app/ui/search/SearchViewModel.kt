package com.keepsake.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepsake.app.ai.AiService
import com.keepsake.app.data.local.datastore.KeepsakeDataStore
import com.keepsake.app.data.repository.AreaRepository
import com.keepsake.app.data.repository.ItemRepository
import com.keepsake.app.data.repository.RoomRepository
import com.keepsake.app.domain.model.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchResult(
    val item: Item,
    val location: String = ""
)

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val aiAnswer: String? = null,
    val aiCitedIds: List<String> = emptyList(),
    val isLoadingAi: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val itemRepo: ItemRepository,
    private val roomRepo: RoomRepository,
    private val areaRepo: AreaRepository,
    private val aiService: AiService,
    private val dataStore: KeepsakeDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    @OptIn(FlowPreview::class)
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, aiAnswer = null, aiCitedIds = emptyList()) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            _uiState.update { it.copy(isSearching = true) }
            val items = itemRepo.search(query)
            val results = items.map { item ->
                val area = areaRepo.getById(item.areaId)
                val room = area?.let { roomRepo.getById(it.roomId) }
                SearchResult(
                    item = item,
                    location = listOfNotNull(room?.name, area?.name).joinToString(" > ")
                )
            }
            _uiState.update { it.copy(results = results, isSearching = false) }
        }
    }

    fun clearQuery() {
        searchJob?.cancel()
        _uiState.update { it.copy(query = "", results = emptyList(), aiAnswer = null) }
    }

    fun searchWithAi() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAi = true, error = null) }
            val provider = dataStore.aiProvider.first()
            val apiKey = if (provider == "deepseek") dataStore.aiDeepseekKey.first() else dataStore.aiApiKey.first()

            val contextItems = itemRepo.search("") // Get recent items for context
                .take(30)
                .map { item ->
                    val area = areaRepo.getById(item.areaId)
                    val room = area?.let { roomRepo.getById(it.roomId) }
                    mapOf(
                        "id" to item.id,
                        "name" to item.name,
                        "qty" to item.qty.toString(),
                        "location" to listOfNotNull(room?.name, area?.name).joinToString(" > "),
                        "notes" to (item.notes ?: "")
                    )
                }

            when (val result = aiService.searchAnswer(query, contextItems, provider, apiKey)) {
                is com.keepsake.app.ai.AiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingAi = false,
                            aiAnswer = result.data.answer,
                            aiCitedIds = result.data.citedIds
                        )
                    }
                }
                is com.keepsake.app.ai.AiResult.Error -> {
                    _uiState.update { it.copy(isLoadingAi = false, error = result.message) }
                }
            }
        }
    }
}
