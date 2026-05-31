package com.keepsake.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.keepsake.app.domain.model.Room
import com.keepsake.app.ui.components.ConfirmDialog
import com.keepsake.app.ui.components.EmptyState
import com.keepsake.app.ui.components.SwipeToDismissCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRoomClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Room?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keepsake") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加房间")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.rooms.isEmpty() -> EmptyState(
                    icon = Icons.Default.DoorFront,
                    message = "还没有房间，点击右下角添加"
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.rooms, key = { it.id }) { room ->
                        RoomCard(
                            room = room,
                            isEditing = uiState.editingRoomId == room.id,
                            editingName = uiState.editingName,
                            onNameChange = { viewModel.updateEditingName(it) },
                            onNameClick = { viewModel.startEditing(room) },
                            onCommitRename = { viewModel.commitRename() },
                            onCancelEdit = { viewModel.cancelEditing() },
                            onClick = { onRoomClick(room.id) },
                            onDelete = { deleteTarget = room }
                        )
                    }
                }
            }
        }
    }

    // Add dialog
    if (uiState.showAddDialog) {
        AddRoomDialog(
            onDismiss = { viewModel.dismissAddDialog() },
            onConfirm = { name -> viewModel.createRoom(name) }
        )
    }

    // Delete confirm
    deleteTarget?.let { room ->
        ConfirmDialog(
            title = "删除房间",
            message = "确认删除「${room.name}」？该房间下的所有区域和物品也会被删除。",
            confirmLabel = "删除",
            isDangerous = true,
            onConfirm = {
                viewModel.deleteRoom(room.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun RoomCard(
    room: Room,
    isEditing: Boolean,
    editingName: String,
    onNameChange: (String) -> Unit,
    onNameClick: () -> Unit,
    onCommitRename: () -> Unit,
    onCancelEdit: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    SwipeToDismissCard(onDelete = onDelete) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DoorFront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editingName,
                            onValueChange = onNameChange,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row {
                            TextButton(onClick = onCommitRename) { Text("确定") }
                            TextButton(onClick = onCancelEdit) { Text("取消") }
                        }
                    } else {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.clickable { onNameClick() }
                        )
                        Text(
                            text = "${room.areaCount} 个区域",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加房间") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 60) name = it },
                    label = { Text("房间名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text("预设名称", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                val presets = listOf("厨房", "客厅", "卧室", "卫生间", "阳台", "储物间", "书房", "餐厅")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = name == preset,
                            onClick = { name = preset },
                            label = { Text(preset) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) {
                Text("确定")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
