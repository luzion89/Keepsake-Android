package com.keepsake.app.ui.area

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.keepsake.app.domain.model.Item
import com.keepsake.app.ui.components.ConfirmDialog
import com.keepsake.app.ui.components.EmptyState
import com.keepsake.app.ui.components.ExpiryBadge
import com.keepsake.app.ui.components.SwipeToDismissCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaScreen(
    viewModel: AreaViewModel,
    onItemClick: (String) -> Unit,
    onCaptureClick: () -> Unit,
    onTextInputClick: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Item?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.area?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.area == null -> EmptyState(Icons.Default.Warning, "区域不存在")
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // CTA Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AssistChip(
                                onClick = onTextInputClick,
                                label = { Text("文字输入") },
                                leadingIcon = { Icon(Icons.Default.Edit, null, Modifier.size(18.dp)) }
                            )
                            AssistChip(
                                onClick = onCaptureClick,
                                label = { Text("拍照") },
                                leadingIcon = { Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp)) }
                            )
                        }
                    }

                    // Manual add form
                    item {
                        if (uiState.showAddForm) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    OutlinedTextField(
                                        value = uiState.addName,
                                        onValueChange = { viewModel.updateAddName(it) },
                                        label = { Text("物品名称") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = uiState.addQty,
                                        onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.updateAddQty(it) },
                                        label = { Text("数量") },
                                        singleLine = true,
                                        modifier = Modifier.width(120.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = { viewModel.addItem() }) { Text("添加") }
                                }
                            }
                        } else {
                            TextButton(
                                onClick = { viewModel.toggleAddForm() },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                                Text("快速添加")
                            }
                        }
                    }

                    // Item list header
                    item {
                        Text(
                            "${uiState.items.size} 件物品",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Item list
                    items(uiState.items, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            isEditing = uiState.editingItemId == item.id,
                            editingName = uiState.editingName,
                            onNameChange = { viewModel.updateEditingName(it) },
                            onNameClick = { viewModel.startEditing(item) },
                            onCommitRename = { viewModel.commitRename() },
                            onCancelEdit = { viewModel.cancelEditing() },
                            onClick = { onItemClick(item.id) },
                            onQtyDelta = { d -> viewModel.qtyDelta(item.id, d) },
                            onDelete = { deleteTarget = item }
                        )
                    }

                    // Photos section
                    if (uiState.photos.isNotEmpty()) {
                        item {
                            Text(
                                "照片",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.photos.size) { index ->
                                    AsyncImage(
                                        model = uiState.photos[index].blobUri,
                                        contentDescription = "照片",
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable { viewModel.showPhotoAtIndex(index) },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { item ->
        ConfirmDialog(
            title = "删除物品",
            message = "确认删除"${item.name}"？",
            isDangerous = true,
            onConfirm = { viewModel.deleteItem(item.id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun ItemCard(
    item: Item,
    isEditing: Boolean,
    editingName: String,
    onNameChange: (String) -> Unit,
    onNameClick: () -> Unit,
    onCommitRename: () -> Unit,
    onCancelEdit: () -> Unit,
    onClick: () -> Unit,
    onQtyDelta: (Int) -> Unit,
    onDelete: () -> Unit
) {
    SwipeToDismissCard(onDelete = onDelete) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExpiryBadge(expiresAt = item.expiresAt)
                        if (item.source == "ai") {
                            Spacer(Modifier.width(4.dp))
                            Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small) {
                                Text("AI", modifier = Modifier.padding(horizontal = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                    if (isEditing) {
                        OutlinedTextField(value = editingName, onValueChange = onNameChange, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Row {
                            TextButton(onClick = onCommitRename) { Text("确定") }
                            TextButton(onClick = onCancelEdit) { Text("取消") }
                        }
                    } else {
                        Text(item.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.clickable { onNameClick() })
                    }
                    Text(
                        "${item.qty}${item.unit ?: "件"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column {
                    IconButton(onClick = { onQtyDelta(1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onQtyDelta(-1) }, modifier = Modifier.size(32.dp), enabled = item.qty > 0) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
