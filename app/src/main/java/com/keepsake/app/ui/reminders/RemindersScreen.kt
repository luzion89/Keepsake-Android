package com.keepsake.app.ui.reminders

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
import com.keepsake.app.ui.components.EmptyState
import com.keepsake.app.ui.components.ExpiryBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    onItemClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提醒") },
                actions = {
                    IconButton(onClick = { viewModel.scanReminders() }) {
                        Icon(Icons.Default.Refresh, "刷新")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.reminders.isEmpty() -> EmptyState(
                    icon = Icons.Default.NotificationsNone,
                    message = "暂无提醒"
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.reminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onViewItem = { onItemClick(reminder.item.id) },
                            onDismiss = { viewModel.dismiss(reminder.rule.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: TriggeredReminder,
    onViewItem: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                reminder.reason.contains("已过期") -> MaterialTheme.colorScheme.errorContainer
                reminder.reason.contains("库存") -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        reminder.rule.kind == "expiry" -> Icons.Default.Warning
                        reminder.rule.kind == "low_stock" -> Icons.Default.Inventory2
                        else -> Icons.Default.Refresh
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(reminder.item.name, style = MaterialTheme.typography.titleSmall)
                    Text(reminder.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    reminder.rule.note?.let { Text("备注: $it", style = MaterialTheme.typography.bodySmall) }
                }
                ExpiryBadge(expiresAt = reminder.item.expiresAt)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onViewItem) { Text("查看物品") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismiss) { Text("已读") }
            }
        }
    }
}
