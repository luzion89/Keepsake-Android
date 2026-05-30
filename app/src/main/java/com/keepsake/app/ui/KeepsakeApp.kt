package com.keepsake.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keepsake.app.ui.area.AreaScreen
import com.keepsake.app.ui.area.AreaViewModel
import com.keepsake.app.ui.capture.CaptureScreen
import com.keepsake.app.ui.home.HomeScreen
import com.keepsake.app.ui.home.HomeViewModel
import com.keepsake.app.ui.item.ItemScreen
import com.keepsake.app.ui.item.ItemViewModel
import com.keepsake.app.ui.navigation.Screen
import com.keepsake.app.ui.navigation.bottomNavItems
import com.keepsake.app.ui.reminders.RemindersScreen
import com.keepsake.app.ui.reminders.RemindersViewModel
import com.keepsake.app.ui.room.RoomScreen
import com.keepsake.app.ui.room.RoomViewModel
import com.keepsake.app.ui.search.SearchScreen
import com.keepsake.app.ui.search.SearchViewModel
import com.keepsake.app.ui.settings.SettingsScreen
import com.keepsake.app.ui.settings.SettingsViewModel
import com.keepsake.app.ui.textinput.TextInputScreen
import com.keepsake.app.ui.textinput.TextInputViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepsakeApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = setOf(
        Screen.Home.route, Screen.Search.route, Screen.Settings.route, Screen.Reminders.route
    )
    val showBottomBar = currentDestination?.route in bottomBarScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val vm: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = vm,
                    onRoomClick = { roomId -> navController.navigate(Screen.Room.createRoute(roomId)) }
                )
            }

            composable(
                route = Screen.Room.route,
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
                val vm: RoomViewModel = hiltViewModel()
                LaunchedEffect(roomId) { vm.load(roomId) }
                RoomScreen(
                    viewModel = vm,
                    onAreaClick = { areaId -> navController.navigate(Screen.Area.createRoute(areaId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Area.route,
                arguments = listOf(navArgument("areaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val areaId = backStackEntry.arguments?.getString("areaId") ?: return@composable
                val vm: AreaViewModel = hiltViewModel()
                LaunchedEffect(areaId) { vm.load(areaId) }
                AreaScreen(
                    viewModel = vm,
                    onItemClick = { itemId -> navController.navigate(Screen.Item.createRoute(itemId)) },
                    onCaptureClick = { navController.navigate(Screen.Capture.createRoute(areaId)) },
                    onTextInputClick = { navController.navigate(Screen.TextInput.createRoute(areaId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Capture.route,
                arguments = listOf(navArgument("areaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val areaId = backStackEntry.arguments?.getString("areaId") ?: return@composable
                CaptureScreen(
                    areaId = areaId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.TextInput.route,
                arguments = listOf(navArgument("areaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val areaId = backStackEntry.arguments?.getString("areaId") ?: return@composable
                val vm: TextInputViewModel = hiltViewModel()
                LaunchedEffect(areaId) { vm.load(areaId) }
                TextInputScreen(
                    viewModel = vm,
                    onItemEdit = { itemId -> navController.navigate(Screen.Item.createRoute(itemId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Item.route,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                val vm: ItemViewModel = hiltViewModel()
                LaunchedEffect(itemId) { vm.load(itemId) }
                ItemScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Search.route) {
                val vm: SearchViewModel = hiltViewModel()
                SearchScreen(
                    viewModel = vm,
                    onItemClick = { itemId -> navController.navigate(Screen.Item.createRoute(itemId)) }
                )
            }

            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = vm)
            }

            composable(Screen.Reminders.route) {
                val vm: RemindersViewModel = hiltViewModel()
                RemindersScreen(
                    viewModel = vm,
                    onItemClick = { itemId -> navController.navigate(Screen.Item.createRoute(itemId)) }
                )
            }
        }
    }
}
