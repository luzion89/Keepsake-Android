package com.keepsake.app.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepsake.app.data.repository.ItemRepository
import com.keepsake.app.data.repository.ReminderRepository
import com.keepsake.app.domain.model.Item
import com.keepsake.app.domain.model.ReminderRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TriggeredReminder(
    val rule: ReminderRule,
    val item: Item,
    val reason: String
)

data class RemindersUiState(
    val reminders: List<TriggeredReminder> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val reminderRepo: ReminderRepository,
    private val itemRepo: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        scanReminders()
        // Auto-refresh every 60 seconds
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                scanReminders()
            }
        }
    }

    fun scanReminders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val rules = reminderRepo.getAll()
            val now = System.currentTimeMillis()
            val triggered = mutableListOf<TriggeredReminder>()

            for (rule in rules) {
                val item = itemRepo.getById(rule.itemId) ?: continue
                if (item.deleted) continue

                val oneHourAgo = now - 3600_000
                if (rule.lastFiredAt != null && rule.lastFiredAt > oneHourAgo) continue

                when (rule.kind) {
                    "expiry" -> {
                        val threshold = rule.thresholdAt ?: continue
                        val remaining = (item.expiresAt ?: continue) - now
                        if (remaining <= threshold) {
                            val daysLeft = remaining / (24 * 3600_000)
                            triggered.add(TriggeredReminder(rule, item, if (daysLeft <= 0) "已过期" else "还有${daysLeft}天过期"))
                        }
                    }
                    "low_stock" -> {
                        val thresholdQty = rule.thresholdQty ?: continue
                        if (item.qty <= thresholdQty) {
                            triggered.add(TriggeredReminder(rule, item, "库存不足（当前${item.qty}，需要至少${thresholdQty}件）"))
                        }
                    }
                    "recheck" -> {
                        val interval = rule.thresholdAt ?: continue
                        if ((rule.lastFiredAt ?: 0) + interval <= now) {
                            triggered.add(TriggeredReminder(rule, item, "需要复查"))
                        }
                    }
                }
            }

            _uiState.update { it.copy(reminders = triggered, isLoading = false) }
        }
    }

    fun dismiss(id: String) {
        viewModelScope.launch {
            reminderRepo.updateFired(id)
            scanReminders()
        }
    }
}
