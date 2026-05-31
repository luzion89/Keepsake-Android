package com.keepsake.app.ui.item

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import com.keepsake.app.ui.components.ConfirmDialog
import com.keepsake.app.ui.components.ExpiryBadge
import com.keepsake.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(viewModel: ItemViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val item = state.item

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "编辑" else "物品", fontFamily = SerifFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink) },
                navigationIcon = { IconButton(onClick = { if (state.isEditing) viewModel.toggleEdit() else onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Ink) } },
                actions = {
                    if (!state.isEditing) {
                        IconButton(onClick = viewModel::toggleEdit) { Icon(Icons.Default.Edit, "编辑", tint = Ink) }
                        IconButton(onClick = viewModel::showDeleteDialog) { Icon(Icons.Default.Delete, "删除", tint = Ink) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        }
    ) { padding ->
        if (state.isLoading) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Rosewood) }
        else if (item == null) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("物品不存在", color = InkMuted) }
        else if (state.isEditing) EditContent(viewModel, state, Modifier.fillMaxSize().padding(padding))
        else ViewContent(viewModel, state, Modifier.fillMaxSize().padding(padding))
    }

    if (state.deleteDialogVisible) {
        ConfirmDialog(title = "删除物品", message = "确认删除「${item?.name}」？", isDangerous = true,
            onConfirm = { viewModel.deleteItem(); viewModel.dismissDeleteDialog(); onBack() }, onDismiss = viewModel::dismissDeleteDialog)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ViewContent(vm: ItemViewModel, state: ItemUiState, mod: Modifier) {
    val item = state.item!!
    LazyColumn(mod.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExpiryBadge(expiresAt = item.expiresAt); Spacer(Modifier.width(8.dp))
                Text(item.name, fontFamily = SerifFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Ink)
            }
        }
        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.qtyDelta(-1) }, modifier = Modifier.size(44.dp)) { Icon(Icons.Default.Remove, null, Modifier.size(20.dp), tint = Ink) }
                    Text("${item.qty} ${item.unit ?: "件"}", fontFamily = SerifFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Ink, modifier = Modifier.padding(horizontal = 20.dp))
                    IconButton(onClick = { vm.qtyDelta(1) }, modifier = Modifier.size(44.dp)) { Icon(Icons.Default.Add, null, Modifier.size(20.dp), tint = Ink) }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(16.dp)) {
                    DetailRow("来源", item.source)
                    item.confidence?.let { DetailRow("置信度", "${(it * 100).toInt()}%") }
                    item.expiresAt?.let { DetailRow("过期", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))) }
                    DetailRow("创建", SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.createdAt)))
                }
            }
        }
        item.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("备注", fontSize = 12.sp, color = InkMuted); Spacer(Modifier.height(4.dp))
                        Text(notes, fontSize = 14.sp, color = Ink)
                    }
                }
            }
        }
        if (item.tags.isNotEmpty()) {
            item { FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) { item.tags.forEach { SuggestionChip(onClick = {}, label = { Text(it, fontSize = 12.sp) }) } } }
        }
        if (state.reminders.isNotEmpty()) {
            item { Text("提醒规则", fontSize = 12.sp, color = InkMuted, modifier = Modifier.padding(top = 8.dp)) }
            items(state.reminders) { r -> ReminderRow(r, { vm.removeReminder(r.id) }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditContent(vm: ItemViewModel, state: ItemUiState, mod: Modifier) {
    LazyColumn(mod.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(state.editName, { vm.updateEditName(it) }, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedFieldColors(Rosewood))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(state.editQty, { vm.updateEditQty(it) }, label = { Text("数量") }, singleLine = true, modifier = Modifier.weight(1f), colors = outlinedFieldColors(Rosewood))
                        OutlinedTextField(state.editUnit, { vm.updateEditUnit(it) }, label = { Text("单位") }, singleLine = true, modifier = Modifier.weight(1f), colors = outlinedFieldColors(Rosewood))
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(state.editNotes, { vm.updateEditNotes(it) }, label = { Text("备注") }, minLines = 3, modifier = Modifier.fillMaxWidth(), colors = outlinedFieldColors(Rosewood))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(state.editTags, { vm.updateEditTags(it) }, label = { Text("标签（逗号分隔）") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedFieldColors(Rosewood))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { vm.saveEdit() }, colors = ButtonDefaults.buttonColors(containerColor = Rosewood), shape = RoundedCornerShape(12.dp)) { Text("保存") }
                OutlinedButton(onClick = { vm.toggleEdit() }, shape = RoundedCornerShape(12.dp)) { Text("取消") }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 2.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = InkMuted); Text(value, fontSize = 13.sp, color = Ink)
    }
}

@Composable
private fun ReminderRow(rule: com.keepsake.app.domain.model.ReminderRule, onRemove: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(when(rule.kind){"expiry"->"过期提醒";"low_stock"->"低库存提醒";"recheck"->"复查提醒";else->rule.kind}, fontSize = 13.sp, color = Ink)
                rule.thresholdAt?.let { Text("阈值: ${it/(86400000)}天", fontSize = 12.sp, color = InkMuted) }
                rule.thresholdQty?.let { Text("阈值: ${it}件", fontSize = 12.sp, color = InkMuted) }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = InkMuted) }
        }
    }
}
