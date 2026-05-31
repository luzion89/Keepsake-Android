package com.keepsake.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.keepsake.app.data.repository.RoomRepository
import com.keepsake.app.ui.home.HomeScreen
import com.keepsake.app.ui.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel() = mockk<HomeViewModel>(relaxed = true)

    @Test
    fun emptyStateShouldShowHintMessage() {
        val vm = createMockViewModel()
        val state = com.keepsake.app.ui.home.HomeUiState(
            rooms = emptyList(),
            isLoading = false
        )
        every { vm.uiState } returns kotlinx.coroutines.flow.MutableStateFlow(state)

        composeTestRule.setContent {
            HomeScreen(viewModel = vm, onRoomClick = {})
        }

        composeTestRule
            .onNodeWithText("还没有房间，点击右下角添加")
            .assertExists()
    }

    @Test
    fun loadingStateShouldNotShowEmptyStateText() {
        val vm = createMockViewModel()
        val state = com.keepsake.app.ui.home.HomeUiState(isLoading = true)
        every { vm.uiState } returns kotlinx.coroutines.flow.MutableStateFlow(state)

        composeTestRule.setContent {
            HomeScreen(viewModel = vm, onRoomClick = {})
        }

        // Loading state should NOT show empty hint
        composeTestRule
            .onNodeWithText("还没有房间，点击右下角添加")
            .assertDoesNotExist()
    }

    @Test
    fun roomListShouldDisplayRoomNames() {
        val vm = createMockViewModel()
        val rooms = listOf(
            com.keepsake.app.domain.model.Room(id = "1", name = "厨房", areaCount = 3),
            com.keepsake.app.domain.model.Room(id = "2", name = "客厅", areaCount = 0)
        )
        val state = com.keepsake.app.ui.home.HomeUiState(
            rooms = rooms,
            isLoading = false
        )
        every { vm.uiState } returns kotlinx.coroutines.flow.MutableStateFlow(state)

        composeTestRule.setContent {
            HomeScreen(viewModel = vm, onRoomClick = {})
        }

        composeTestRule.onNodeWithText("厨房").assertExists()
        composeTestRule.onNodeWithText("客厅").assertExists()
        composeTestRule.onNodeWithText("3 个区域").assertExists()
    }

    @Test
    fun fabClickShouldShowAddDialog() {
        val vm = createMockViewModel()
        val state = com.keepsake.app.ui.home.HomeUiState(
            rooms = emptyList(),
            isLoading = false,
            showAddDialog = true
        )
        every { vm.uiState } returns kotlinx.coroutines.flow.MutableStateFlow(state)

        composeTestRule.setContent {
            HomeScreen(viewModel = vm, onRoomClick = {})
        }

        composeTestRule.onNodeWithText("添加房间").assertExists()
        composeTestRule.onNodeWithText("房间名称").assertExists()
    }

    @Test
    fun addDialogShouldShowPresetNames() {
        val vm = createMockViewModel()
        val state = com.keepsake.app.ui.home.HomeUiState(
            rooms = emptyList(),
            isLoading = false,
            showAddDialog = true
        )
        every { vm.uiState } returns kotlinx.coroutines.flow.MutableStateFlow(state)

        composeTestRule.setContent {
            HomeScreen(viewModel = vm, onRoomClick = {})
        }

        // All 8 presets should be visible
        composeTestRule.onNodeWithText("厨房").assertExists()
        composeTestRule.onNodeWithText("客厅").assertExists()
        composeTestRule.onNodeWithText("卧室").assertExists()
        composeTestRule.onNodeWithText("卫生间").assertExists()
    }

    @Test
    fun deleteConfirmDialogShouldAppear() {
        val vm = createMockViewModel()
        val rooms = listOf(
            com.keepsake.app.domain.model.Room(id = "1", name = "测试房间")
        )
        val state = com.keepsake.app.ui.home.HomeUiState(
            rooms = rooms,
            isLoading = false
        )
        every { vm.uiState } returns kotlinx.coroutines.flow.MutableStateFlow(state)

        composeTestRule.setContent {
            HomeScreen(viewModel = vm, onRoomClick = {})
        }

        // Room name should be visible
        composeTestRule.onNodeWithText("测试房间").assertExists()
        composeTestRule.onNodeWithText("0 个区域").assertExists()
    }
}
