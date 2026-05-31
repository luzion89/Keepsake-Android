package com.keepsake.app.ui.room

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepsake.app.domain.model.Area
import com.keepsake.app.ui.components.ConfirmDialog
import com.keepsake.app.ui.components.EmptyState
import com.keepsake.app.ui.components.SwipeToDismissCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomScreen(
    viewModel: RoomViewModel,
    onAreaClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Area?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.room?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, "添加区域")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.room == null -> EmptyState(Icons.Default.Warning, "房间不存在")
                uiState.areas.isEmpty() -> EmptyState(Icons.Default.Category, "还没有区域，点击右下角添加")
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.areas, key = { it.id }) { area ->
                        AreaCard(
                            area = area,
                            isEditing = uiState.editingAreaId == area.id,
                            editingName = uiState.editingName,
                            onNameChange = { viewModel.updateEditingName(it) },
                            onNameClick = { viewModel.startEditing(area) },
                            onCommitRename = { viewModel.commitRename() },
                            onCancelEdit = { viewModel.cancelEditing() },
                            onClick = { onAreaClick(area.id) },
                            onDelete = { deleteTarget = area }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        var name by remember { mutableStateOf("") }
        val presets = listOf("洗手台柜子", "墙壁柜", "电视柜", "抽屉", "沙发底下", "床底下", "衣柜", "门边角落")

        AlertDialog(
            onDismissRequest = { viewModel.dismissAddDialog() },
            title = { Text("添加区域") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 80) name = it },
                        label = { Text("区域名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
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
                TextButton(onClick = { viewModel.createArea(name.trim()) }, enabled = name.isNotBlank()) {
                    Text("确定")
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.dismissAddDialog() }) { Text("取消") } }
        )
    }

    deleteTarget?.let { area ->
        ConfirmDialog(
            title = "删除区域",
            message = "确认删除「${area.name}」？该区域下的所有物品也会被删除。",
            confirmLabel = "删除",
            isDangerous = true,
            onConfirm = { viewModel.deleteArea(area.id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun AreaCard(
    area: Area,
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
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Category, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    if (isEditing) {
                        OutlinedTextField(value = editingName, onValueChange = onNameChange, singleLine = true)
                        Row {
                            TextButton(onClick = onCommitRename) { Text("确定") }
                            TextButton(onClick = onCancelEdit) { Text("取消") }
                        }
                    } else {
                        Text(area.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.clickable { onNameClick() })
                        Text("${area.itemCount} 件物品", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
