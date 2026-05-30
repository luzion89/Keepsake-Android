package com.keepsake.app.ui.search

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onItemClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("搜索") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search input
            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    placeholder = { Text("搜索物品名称、备注、标签...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (uiState.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearQuery() }) {
                                Icon(Icons.Default.Close, "清除")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // AI button
            item {
                val aiEnabled = true // In real app, check from settings
                if (aiEnabled && uiState.query.isNotBlank()) {
                    OutlinedButton(
                        onClick = { viewModel.searchWithAi() },
                        enabled = !uiState.isLoadingAi,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (uiState.isLoadingAi) "AI 思考中..." else "AI 智能搜索")
                    }
                }
            }

            // Error
            uiState.error?.let {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // AI answer
            uiState.aiAnswer?.let { answer ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("AI 回答", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.height(4.dp))
                            Text(answer, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Loading
            if (uiState.isSearching) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            // Results
            if (uiState.results.isNotEmpty()) {
                item {
                    Text("${uiState.results.size} 个结果", style = MaterialTheme.typography.titleSmall)
                }
                items(uiState.results) { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onItemClick(result.item.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(result.item.name, style = MaterialTheme.typography.bodyLarge)
                                Text(result.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${result.item.qty}${result.item.unit ?: "件"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Empty state
            if (uiState.query.isNotBlank() && !uiState.isSearching && uiState.results.isEmpty() && uiState.aiAnswer == null) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("未找到匹配物品", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
