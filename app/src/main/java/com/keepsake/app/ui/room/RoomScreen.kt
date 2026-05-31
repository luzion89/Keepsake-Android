package com.keepsake.app.ui.room

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keepsake.app.domain.model.Area
import com.keepsake.app.ui.components.ConfirmDialog
import com.keepsake.app.ui.components.EmptyState
import com.keepsake.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomScreen(
    viewModel: RoomViewModel,
    onAreaClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Area?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DoorFront, null, Modifier.size(20.dp), tint = Ink)
                        Spacer(Modifier.width(8.dp))
                        Text(state.room?.name ?: "", fontFamily = SerifFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Ink) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        },
        floatingActionButton = {
            var showSheet by remember { mutableStateOf(false) }
            var name by remember { mutableStateOf("") }
            val presets = listOf("洗手台柜子", "墙壁柜", "抽屉", "沙发底下", "床底下", "衣柜", "门边角落", "电视柜")

            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = Rosewood,
                contentColor = Paper,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Default.Add, "添加区域") }

            if (showSheet) {
                AlertDialog(
                    onDismissRequest = { showSheet = false },
                    containerColor = CardBg,
                    shape = RoundedCornerShape(12.dp),
                    title = { Text("添加区域", fontFamily = SerifFont, fontWeight = FontWeight.Bold, color = Ink) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = name, onValueChange = { if (it.length <= 80) name = it },
                                label = { Text("区域名称", color = InkMuted) },
                                singleLine = true, modifier = Modifier.fillMaxWidth(),
                                colors = outlinedFieldColors(focusedBorderColor = Rosewood)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("预设名称", style = MaterialTheme.typography.labelMedium.copy(color = InkMuted, fontFamily = SansFont, fontSize = 12.sp))
                            Spacer(Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                presets.forEach { p ->
                                    FilterChip(selected = name == p, onClick = { name = p },
                                        label = { Text(p, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Rosewood.copy(alpha = 0.15f), selectedLabelColor = Rosewood))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.createArea(name.trim()); name = ""; showSheet = false }, enabled = name.isNotBlank()) {
                            Text("确定", color = if (name.isNotBlank()) Rosewood else InkMuted)
                        }
                    },
                    dismissButton = { TextButton(onClick = { showSheet = false }) { Text("取消", color = InkMuted) } }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Rosewood)
                state.room == null -> EmptyState(Icons.Default.Warning, "房间不存在")
                state.areas.isEmpty() -> EmptyState(Icons.Default.Category, "还没有区域")
                else -> Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    Text("${state.areas.size} 个区域", fontSize = 12.sp, color = InkMuted, fontFamily = SansFont,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            state.areas.forEachIndexed { idx, area ->
                                AreaRow(area = area,
                                    isEditing = state.editingAreaId == area.id,
                                    editName = state.editingName,
                                    onNameChange = { viewModel.updateEditingName(it) },
                                    onStartEdit = { viewModel.startEditing(area) },
                                    onCommitRename = { viewModel.commitRename() },
                                    onCancelEdit = { viewModel.cancelEditing() },
                                    onClick = { onAreaClick(area.id) },
                                    onDelete = { deleteTarget = area }
                                )
                                if (idx < state.areas.lastIndex) Divider(color = BorderSubtle, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { area ->
        ConfirmDialog(
            title = "删除区域", message = "确认删除「${area.name}」？该区域下的所有物品也会被删除。",
            isDangerous = true,
            onConfirm = { viewModel.deleteArea(area.id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AreaRow(
    area: Area, isEditing: Boolean, editName: String,
    onNameChange: (String) -> Unit, onStartEdit: () -> Unit,
    onCommitRename: () -> Unit, onCancelEdit: () -> Unit,
    onClick: () -> Unit, onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false }
    )
    val focusRequester = remember { FocusRequester() }
    val fm = LocalFocusManager.current

    SwipeToDismissBox(
        state = dismissState, backgroundContent = {
            Box(Modifier.fillMaxSize().background(Color(0xFFD32F2F), RoundedCornerShape(12.dp)).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text("删除", color = Color.White, fontSize = 12.sp); Spacer(Modifier.width(4.dp)); Icon(Icons.Default.Delete, "删除", tint = Color.White, modifier = Modifier.size(16.dp)) }
            }
        }, enableDismissFromStartToEnd = false, enableDismissFromEndToStart = true
    ) {
        Row(Modifier.fillMaxWidth().background(CardBg).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Category, null, Modifier.size(20.dp), tint = InkMuted)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                if (isEditing) {
                    BasicTextField(value = editName, onValueChange = onNameChange, singleLine = true,
                        textStyle = TextStyle(fontFamily = SerifFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Ink),
                        cursorBrush = SolidColor(Rosewood),
                        modifier = Modifier.focusRequester(focusRequester).fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(InkVeryFaint).padding(horizontal = 8.dp, vertical = 4.dp))
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    Row {
                        TextButton(onClick = { fm.clearFocus(); onCommitRename() }) { Text("确定", color = Rosewood, fontSize = 12.sp) }
                        TextButton(onClick = { fm.clearFocus(); onCancelEdit() }) { Text("取消", color = InkMuted, fontSize = 12.sp) }
                    }
                } else {
                    Text(area.name, fontFamily = SerifFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Ink,
                        modifier = Modifier.clickable { onStartEdit() })
                    Text("${area.itemCount} 件物品", fontSize = 12.sp, color = InkMuted)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = InkMuted, modifier = Modifier.size(16.dp))
        }
    }
}
