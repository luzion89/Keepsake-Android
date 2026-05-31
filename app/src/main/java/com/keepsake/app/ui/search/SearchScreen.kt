package com.keepsake.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
fun SearchScreen(viewModel: SearchViewModel, onItemClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("搜索", fontFamily = SerifFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                OutlinedTextField(value = state.query, onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("搜索物品名称、备注、标签...", color = InkMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = InkMuted) },
                    trailingIcon = { if (state.query.isNotEmpty()) IconButton(onClick = viewModel::clearQuery) { Icon(Icons.Default.Close, "清除", tint = InkMuted) } },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedFieldColors(Rosewood))
            }

            if (state.query.isNotBlank()) {
                item {
                    OutlinedButton(onClick = viewModel::searchWithAi, enabled = !state.isLoadingAi, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Rosewood)) {
                        if (state.isLoadingAi) { CircularProgressIndicator(Modifier.size(16.dp), color = Rosewood, strokeWidth = 2.dp) } else Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp)); Text(if (state.isLoadingAi) "AI 思考中..." else "AI 智能搜索")
                    }
                }
            }

            state.error?.let {
                item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp)) { Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp) } }
            }

            state.aiAnswer?.let { answer ->
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Rosewood.copy(alpha = 0.08f)), elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("AI 回答", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Rosewood)
                            Spacer(Modifier.height(4.dp)); Text(answer, fontSize = 14.sp, color = Ink)
                        }
                    }
                }
            }

            if (state.isSearching) item { LinearProgressIndicator(Modifier.fillMaxWidth(), color = Rosewood) }

            if (state.results.isNotEmpty()) {
                item { Text("${state.results.size} 个结果", fontSize = 12.sp, color = InkMuted) }
                items(state.results) { r ->
                    Card(Modifier.fillMaxWidth().clickable { onItemClick(r.item.id) }, shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(r.item.name, fontFamily = SerifFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Ink)
                                Text(r.location, fontSize = 12.sp, color = InkMuted)
                                Text("${r.item.qty}${r.item.unit ?: "件"}", fontSize = 12.sp, color = InkMuted)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = InkMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
