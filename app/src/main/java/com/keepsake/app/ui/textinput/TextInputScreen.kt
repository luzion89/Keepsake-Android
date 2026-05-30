package com.keepsake.app.ui.textinput

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputScreen(
    viewModel: TextInputViewModel,
    onItemEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val draftItems by viewModel.draftItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.areaName.ifBlank { "文字录入" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (draftItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.confirmSave() }, enabled = !uiState.isSaving) {
                            Icon(Icons.Default.Check, "保存")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mode toggle
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("模式: ", style = MaterialTheme.typography.labelMedium)
                    Text(if (uiState.mode == "merge") "合并（追加到现有物品）" else "替换（清空后重新创建）")
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.toggleMode() }) { Text("切换") }
                }
            }

            // Text input
            item {
                OutlinedTextField(
                    value = uiState.text,
                    onValueChange = { viewModel.updateText(it) },
                    label = { Text("描述你新买的/有的物品...") },
                    placeholder = { Text("例如：三瓶洗发水和两盒牙膏，白色沐浴露在牙刷旁边") },
                    minLines = 4,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Parse button
            item {
                Button(
                    onClick = { viewModel.parse() },
                    enabled = !uiState.isParsing && uiState.text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isParsing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("解析中...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("AI 解析")
                    }
                }
            }

            // Error
            uiState.error?.let { err ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(err, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // Draft items
            if (draftItems.isNotEmpty()) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("解析结果 (${draftItems.size}项)", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = { viewModel.addManualDraft() }) { Text("+ 手动添加") }
                    }
                }
                items(draftItems, key = { it.tempId }) { draft ->
                    DraftItemCard(
                        draft = draft,
                        onEdit = { /* edit locally */ },
                        onRemove = { viewModel.removeDraftItem(draft.tempId) }
                    )
                }
            }

            // Saving indicator
            if (uiState.isSaving) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun DraftItemCard(
    draft: ParsedItemWithId,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (draft.isNew) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
                    Text("新增", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            } else {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                    Text("更新", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(draft.item.name.ifBlank { "(未命名)" }, style = MaterialTheme.typography.bodyLarge)
                Text("${draft.item.qty}${draft.item.unit ?: "件"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                draft.item.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, "删除", modifier = Modifier.size(18.dp)) }
        }
    }
}
