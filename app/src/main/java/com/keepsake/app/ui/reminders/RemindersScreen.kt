package com.keepsake.app.ui.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keepsake.app.ui.components.EmptyState
import com.keepsake.app.ui.components.ExpiryBadge
import com.keepsake.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: RemindersViewModel, onItemClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("提醒", fontFamily = SerifFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink) },
                actions = { IconButton(onClick = viewModel::scanReminders) { Icon(Icons.Default.Refresh, "刷新", tint = Ink) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper))
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Rosewood)
                state.reminders.isEmpty() -> EmptyState(Icons.Default.NotificationsNone, "暂无提醒")
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.reminders.forEach { reminder ->
                        item {
                            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(0.dp),
                                colors = CardDefaults.cardColors(containerColor = when {
                                    reminder.reason.contains("已过期") -> Color(0xFFFFEBEE)
                                    reminder.reason.contains("库存") -> Color(0xFFFFF3E0)
                                    else -> CardBg
                                })) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(when{reminder.rule.kind=="expiry"->Icons.Default.Warning;reminder.rule.kind=="low_stock"->Icons.Default.Inventory2;else->Icons.Default.Refresh},
                                            null, tint = if (reminder.reason.contains("已过期")) Color(0xFFB71C1C) else Ink, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(reminder.item.name, fontFamily = SerifFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Ink)
                                            Text(reminder.reason, fontSize = 12.sp, color = InkMuted)
                                            reminder.rule.note?.let { Text(it, fontSize = 12.sp, color = InkMuted) }
                                        }
                                        ExpiryBadge(expiresAt = reminder.item.expiresAt)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { onItemClick(reminder.item.id) }) { Text("查看", color = Rosewood, fontSize = 12.sp) }
                                        TextButton(onClick = { viewModel.dismiss(reminder.rule.id) }) { Text("已读", color = InkMuted, fontSize = 12.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
