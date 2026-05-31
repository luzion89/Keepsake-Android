package com.keepsake.app.ui.item

import androidx.compose.foundation.layout.*
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
import com.keepsake.app.domain.model.Item
import com.keepsake.app.domain.model.ReminderRule
import com.keepsake.app.ui.components.ConfirmDialog
import com.keepsake.app.ui.components.ExpiryBadge
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    viewModel: ItemViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val item = uiState.item

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "编辑物品" else "物品详情") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isEditing) viewModel.toggleEdit()
                        else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (!uiState.isEditing) {
                        IconButton(onClick = { viewModel.toggleEdit() }) {
                            Icon(Icons.Default.Edit, "编辑")
                        }
                        IconButton(onClick = { viewModel.showDeleteDialog() }) {
                            Icon(Icons.Default.Delete, "删除")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (item == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("物品不存在")
            }
        } else if (uiState.isEditing) {
            EditItemContent(viewModel, uiState, Modifier.fillMaxSize().padding(padding))
        } else {
            ViewItemContent(viewModel, uiState, Modifier.fillMaxSize().padding(padding))
        }
    }

    if (uiState.deleteDialogVisible) {
        ConfirmDialog(
            title = "删除物品",
            message = "确认删除「${item?.name}」？",
            isDangerous = true,
            onConfirm = { viewModel.deleteItem(); viewModel.dismissDeleteDialog(); onBack() },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }
}

@Composable
private fun ViewItemContent(
    viewModel: ItemViewModel,
    state: ItemUiState,
    modifier: Modifier
) {
    val item = state.item!!

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Name and expiry
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExpiryBadge(expiresAt = item.expiresAt)
                Spacer(Modifier.width(8.dp))
                Text(item.name, style = MaterialTheme.typography.headlineMedium)
            }
        }

        // Quantity adjustment
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { viewModel.qtyDelta(-1) }) { Icon(Icons.Default.Remove, "减少") }
                    Text(
                        "${item.qty} ${item.unit ?: "件"}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    IconButton(onClick = { viewModel.qtyDelta(1) }) { Icon(Icons.Default.Add, "增加") }
                }
            }
        }

        // Meta info
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("来源", item.source)
                    item.confidence?.let { InfoRow("置信度", "${(it * 100).toInt()}%") }
                    item.expiresAt?.let {
                        InfoRow("过期日期", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)))
                    }
                    InfoRow("创建时间", SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.createdAt)))
                }
            }
        }

        // Notes
        item.notes?.let { notes ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("备注", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Tags
        if (item.tags.isNotEmpty()) {
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    item.tags.forEach { tag ->
                        SuggestionChip(onClick = {}, label = { Text(tag) })
                    }
                }
            }
        }

        // Reminders
        if (state.reminders.isNotEmpty()) {
            item {
                Text("提醒规则", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
            }
            items(state.reminders) { rule ->
                ReminderCard(rule, onRemove = { viewModel.removeReminder(rule.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemContent(
    viewModel: ItemViewModel,
    state: ItemUiState,
    modifier: Modifier
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(value = state.editName, onValueChange = { viewModel.updateEditName(it) }, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = state.editQty, onValueChange = { viewModel.updateEditQty(it) }, label = { Text("数量") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = state.editUnit, onValueChange = { viewModel.updateEditUnit(it) }, label = { Text("单位") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(value = state.editNotes, onValueChange = { viewModel.updateEditNotes(it) }, label = { Text("备注") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = state.editTags, onValueChange = { viewModel.updateEditTags(it) }, label = { Text("标签（逗号分隔）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { viewModel.saveEdit() }) { Text("保存") }
                OutlinedButton(onClick = { viewModel.toggleEdit() }) { Text("取消") }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ReminderCard(rule: ReminderRule, onRemove: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    when (rule.kind) {
                        "expiry" -> "过期提醒"
                        "low_stock" -> "低库存提醒"
                        "recheck" -> "复查提醒"
                        else -> rule.kind
                    },
                    style = MaterialTheme.typography.labelMedium
                )
                rule.thresholdAt?.let { Text("阈值: ${it}天", style = MaterialTheme.typography.bodySmall) }
                rule.thresholdQty?.let { Text("阈值: ${it}件", style = MaterialTheme.typography.bodySmall) }
                rule.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onRemove) { Icon(Icons.Default.Close, "删除") }
        }
    }
}
