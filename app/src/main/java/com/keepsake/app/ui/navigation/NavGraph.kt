package com.keepsake.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Room : Screen("rooms/{roomId}") {
        fun createRoute(roomId: String) = "rooms/$roomId"
    }
    data object Area : Screen("areas/{areaId}") {
        fun createRoute(areaId: String) = "areas/$areaId"
    }
    data object Capture : Screen("areas/{areaId}/capture") {
        fun createRoute(areaId: String) = "areas/$areaId/capture"
    }
    data object TextInput : Screen("areas/{areaId}/text") {
        fun createRoute(areaId: String) = "areas/$areaId/text"
    }
    data object Item : Screen("items/{itemId}") {
        fun createRoute(itemId: String) = "items/$itemId"
    }
    data object Search : Screen("search")
    data object Settings : Screen("settings")
    data object Reminders : Screen("reminders")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "房间", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Search, "搜索", Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItem(Screen.Reminders, "提醒", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    BottomNavItem(Screen.Settings, "设置", Icons.Filled.Settings, Icons.Outlined.Settings)
)
