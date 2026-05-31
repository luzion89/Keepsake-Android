package com.keepsake.app.ui.area

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.keepsake.app.domain.model.Item
import com.keepsake.app.ui.components.ConfirmDialog
import com.keepsake.app.ui.components.EmptyState
import com.keepsake.app.ui.components.ExpiryBadge
import com.keepsake.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaScreen(
    viewModel: AreaViewModel,
    onItemClick: (String) -> Unit,
    onCaptureClick: () -> Unit,
    onTextInputClick: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Item?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.area?.name ?: "", fontFamily = SerifFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Ink) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Rosewood)
                state.area == null -> EmptyState(Icons.Default.Warning, "区域不存在")
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    // CTA buttons
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onTextInputClick, modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rosewood, contentColor = Paper),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) { Icon(Icons.Default.Edit, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("文字输入", fontSize = 13.sp) }
                            OutlinedButton(
                                onClick = onCaptureClick, modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(BorderSubtle))
                            ) { Icon(Icons.Default.CameraAlt, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("拍照", fontSize = 13.sp) }
                        }
                    }

                    // Manual add toggle
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        var addName by remember { mutableStateOf("") }
                        Row(Modifier.padding(horizontal = 16.dp).clickable { expanded = !expanded }, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, Modifier.size(16.dp).padding(end = 4.dp), tint = InkMuted)
                            Text("快速添加", fontSize = 12.sp, color = InkMuted)
                        }
                        if (expanded) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = addName, onValueChange = { addName = it }, label = { Text("名称") }, singleLine = true, modifier = Modifier.weight(2f), colors = outlinedFieldColors(Rosewood))
                                OutlinedTextField(value = "1", onValueChange = {}, label = { Text("数量") }, singleLine = true, modifier = Modifier.weight(1f), colors = outlinedFieldColors(Rosewood))
                                FilledTonalButton(onClick = { if (addName.isNotBlank()) { viewModel.addItem(); addName = "" } },
                                    modifier = Modifier.height(56.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = Rosewood.copy(alpha = 0.15f), contentColor = Rosewood)
                                ) { Text("添加") }
                            }
                        }
                    }

                    // Item list
                    item {
                        Text("${state.items.size} 件物品", fontSize = 12.sp, color = InkMuted, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }

                    if (state.items.isNotEmpty()) {
                        item {
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                Column {
                                    state.items.forEachIndexed { idx, item ->
                                        ItemRow(item = item, onNameClick = {}, onClick = { onItemClick(item.id) },
                                            onQtyDelta = { d -> viewModel.qtyDelta(item.id, d) }, onDelete = { deleteTarget = item })
                                        if (idx < state.items.lastIndex) Divider(color = BorderSubtle, thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }

                    // Photos
                    if (state.photos.isNotEmpty()) {
                        item {
                            Text("照片 (${state.photos.size})", fontSize = 12.sp, color = InkMuted, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.photos.size) { i ->
                                    AsyncImage(model = state.photos[i].blobUri, contentDescription = null,
                                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { item ->
        ConfirmDialog(title = "删除物品", message = "确认删除「${item.name}」？", isDangerous = true,
            onConfirm = { viewModel.deleteItem(item.id); deleteTarget = null }, onDismiss = { deleteTarget = null })
    }
}

@Composable
private fun ItemRow(
    item: Item, onNameClick: () -> Unit, onClick: () -> Unit,
    onQtyDelta: (Int) -> Unit, onDelete: () -> Unit
) {
    var swiped by remember { mutableStateOf(false) }
    Box {
        if (swiped) {
            Row(Modifier.fillMaxSize().background(Color(0xFFD32F2F), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "删除", tint = Color.White) }
            }
        }
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExpiryBadge(expiresAt = item.expiresAt)
                    Spacer(Modifier.width(4.dp))
                    Text(item.name, fontFamily = SerifFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Ink)
                }
                Text("${item.qty}${item.unit ?: "件"}", fontSize = 12.sp, color = InkMuted)
                if (item.source == "ai") { Text("AI", fontSize = 10.sp, color = Rosewood) }
            }
            IconButton(onClick = { onQtyDelta(-1) }, modifier = Modifier.size(32.dp), enabled = item.qty > 0) {
                Icon(Icons.Default.Remove, null, Modifier.size(16.dp), tint = if (item.qty > 0) Ink else BorderSubtle)
            }
            Text("${item.qty}", fontSize = 14.sp, color = Ink, modifier = Modifier.width(28.dp).padding(horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick = { onQtyDelta(1) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = Ink)
            }
        }
    }
}
