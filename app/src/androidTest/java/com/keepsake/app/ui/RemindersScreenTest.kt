package com.keepsake.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.keepsake.app.domain.model.Item
import com.keepsake.app.domain.model.ReminderRule
import com.keepsake.app.ui.reminders.RemindersScreen
import com.keepsake.app.ui.reminders.RemindersUiState
import com.keepsake.app.ui.reminders.RemindersViewModel
import com.keepsake.app.ui.reminders.TriggeredReminder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class RemindersScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyStateShouldShowHint() {
        val vm = mockk<RemindersViewModel>(relaxed = true)
        every { vm.uiState } returns MutableStateFlow(
            RemindersUiState(reminders = emptyList(), isLoading = false)
        )

        composeTestRule.setContent {
            RemindersScreen(viewModel = vm, onItemClick = {})
        }

        composeTestRule.onNodeWithText("暂无提醒").assertExists()
    }

    @Test
    fun loadingStateShouldShowProgress() {
        val vm = mockk<RemindersViewModel>(relaxed = true)
        every { vm.uiState } returns MutableStateFlow(
            RemindersUiState(isLoading = true)
        )

        composeTestRule.setContent {
            RemindersScreen(viewModel = vm, onItemClick = {})
        }

        composeTestRule.onNode(isProgressIndicator()).assertExists()
    }

    @Test
    fun expiredReminderShouldShowWarning() {
        val vm = mockk<RemindersViewModel>(relaxed = true)
        val expiredItem = Item(id = "i1", name = "过期牛奶", expiresAt = 1000)
        val rule = ReminderRule(id = "r1", itemId = "i1", kind = "expiry")
        val triggered = TriggeredReminder(rule = rule, item = expiredItem, reason = "已过期")

        every { vm.uiState } returns MutableStateFlow(
            RemindersUiState(reminders = listOf(triggered), isLoading = false)
        )

        composeTestRule.setContent {
            RemindersScreen(viewModel = vm, onItemClick = {})
        }

        composeTestRule.onNodeWithText("过期牛奶").assertExists()
        composeTestRule.onNodeWithText("已过期").assertExists()
        composeTestRule.onNodeWithText("查看物品").assertExists()
        composeTestRule.onNodeWithText("已读").assertExists()
    }

    @Test
    fun lowStockReminderShouldDisplay() {
        val vm = mockk<RemindersViewModel>(relaxed = true)
        val item = Item(id = "i2", name = "洗发水", qty = 1)
        val rule = ReminderRule(id = "r2", itemId = "i2", kind = "low_stock", thresholdQty = 3)
        val triggered = TriggeredReminder(rule = rule, item = item, reason = "库存不足（当前1，需要至少3件）")

        every { vm.uiState } returns MutableStateFlow(
            RemindersUiState(reminders = listOf(triggered), isLoading = false)
        )

        composeTestRule.setContent {
            RemindersScreen(viewModel = vm, onItemClick = {})
        }

        composeTestRule.onNodeWithText("洗发水").assertExists()
        composeTestRule.onNodeWithText("库存不足（当前1，需要至少3件）").assertExists()
    }
}
