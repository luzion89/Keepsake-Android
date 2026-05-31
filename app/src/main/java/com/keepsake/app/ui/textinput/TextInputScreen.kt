package com.keepsake.app.ui.textinput

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keepsake.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputScreen(viewModel: TextInputViewModel, onItemEdit: (String) -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val drafts by viewModel.draftItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.areaName.ifBlank { "AI 文字录入" }, fontFamily = SerifFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Ink) } },
                actions = { if (drafts.isNotEmpty()) IconButton(onClick = viewModel::confirmSave, enabled = !state.isSaving) { Icon(Icons.Default.Check, "保存", tint = Rosewood) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("模式:", fontSize = 12.sp, color = InkMuted); Spacer(Modifier.width(8.dp))
                    Text(if (state.mode == "merge") "合并（追加到现有物品）" else "替换（清空后重新创建）", fontSize = 13.sp, color = Ink)
                    Spacer(Modifier.width(8.dp)); TextButton(onClick = viewModel::toggleMode) { Text("切换", color = Rosewood, fontSize = 12.sp) }
                }
            }
            item {
                OutlinedTextField(value = state.text, onValueChange = viewModel::updateText,
                    label = { Text("描述你新买的/有的物品...", color = InkMuted) },
                    placeholder = { Text("例如：三瓶洗发水和两盒牙膏，白色沐浴露在牙刷旁边", color = InkMuted.copy(alpha = 0.5f)) },
                    minLines = 4, maxLines = 10, modifier = Modifier.fillMaxWidth(), colors = outlinedFieldColors(Rosewood))
            }
            item {
                Button(onClick = viewModel::parse, enabled = !state.isParsing && state.text.isNotBlank(), modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Rosewood), shape = RoundedCornerShape(12.dp)) {
                    if (state.isParsing) { CircularProgressIndicator(Modifier.size(18.dp), color = Paper, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                    Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(if (state.isParsing) "AI 解析中..." else "AI 解析")
                }
            }
            state.error?.let {
                item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp)) {
                    Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp) } }
            }
            if (drafts.isNotEmpty()) {
                item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("解析结果 (${drafts.size}项)", fontSize = 12.sp, color = InkMuted)
                    TextButton(onClick = viewModel::addManualDraft) { Text("+ 手动添加", color = Rosewood, fontSize = 12.sp) }
                }}
                items(drafts, key = { it.tempId }) { draft ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = if (draft.isNew) Rosewood.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondaryContainer) {
                                Text(if (draft.isNew) "新增" else "更新", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp,
                                    color = if (draft.isNew) Rosewood else MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(draft.item.name.ifBlank { "(未命名)" }, fontFamily = SerifFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Ink)
                                Text("${draft.item.qty}${draft.item.unit ?: "件"}", fontSize = 12.sp, color = InkMuted)
                                draft.item.notes?.takeIf { it.isNotBlank() }?.let { Text(it, fontSize = 11.sp, color = InkMuted, maxLines = 1) }
                            }
                            IconButton(onClick = { viewModel.removeDraftItem(draft.tempId) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Close, null, Modifier.size(16.dp), tint = InkMuted) }
                        }
                    }
                }
            }
            if (state.isSaving) item { LinearProgressIndicator(Modifier.fillMaxWidth(), color = Rosewood) }
        }
    }
}
