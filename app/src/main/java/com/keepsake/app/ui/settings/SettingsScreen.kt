package com.keepsake.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.keepsake.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置", fontFamily = SerifFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Theme Section
            item { SectionHeader("主题") }
            item { SectionCard {
                FilterChipRow(listOf("light" to "浅色", "dark" to "深色", "system" to "跟随系统"), state.theme, { viewModel.setTheme(it) })
            }}

            // Language Section
            item { SectionHeader("语言 / Language") }
            item { SectionCard {
                FilterChipRow(listOf("zh" to "中文", "en" to "English"), state.language, { viewModel.setLanguage(it) })
            }}

            // AI Section
            item { SectionHeader("AI 助手") }
            item { SectionCard {
                var showKey by remember { mutableStateOf(false) }
                var showDsKey by remember { mutableStateOf(false) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("启用 AI", fontSize = 14.sp, color = Ink)
                    Switch(checked = state.aiConfig.enabled, onCheckedChange = { viewModel.setAiEnabled(it) }, colors = SwitchDefaults.colors(checkedTrackColor = Rosewood))
                }
                if (state.aiConfig.enabled) {
                    Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    Text("Provider", fontSize = 12.sp, color = InkMuted)
                    FilterChipRow(listOf("deepseek" to "DeepSeek", "openrouter" to "OpenRouter"), state.aiConfig.provider, { viewModel.setAiProvider(it) })
                    OutlinedTextField(value = if (showDsKey) state.aiConfig.deepseekApiKey else "●●●●●●●●", onValueChange = { viewModel.setAiDeepseekKey(it) },
                        label = { Text("API Key", color = InkMuted) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showDsKey) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = { IconButton(onClick = { showDsKey = !showDsKey }) { Icon(if (showDsKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = InkMuted) } },
                        colors = outlinedFieldColors(Rosewood))
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = { viewModel.testApiConnection(state.aiConfig.provider, state.aiConfig.apiKey.ifBlank { state.aiConfig.deepseekApiKey }) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Rosewood),
                        enabled = !state.isTestingApi) {
                        if (state.isTestingApi) CircularProgressIndicator(Modifier.size(16.dp), color = Rosewood, strokeWidth = 2.dp) else Text("测试连接")
                    }
                    state.apiTestResult?.let { Text(it, fontSize = 12.sp, color = if (it.startsWith("连接成功")) Ink else Color(0xFFD32F2F)) }
                }
            }}

            // Data Section
            item { SectionHeader("本地数据") }
            item { SectionCard {
                InfoRow("设备 ID", state.deviceId)
                Divider(color = BorderSubtle)
                InfoRow("数据统计", "${state.roomCount} 房间, ${state.areaCount} 区域, ${state.itemCount} 物品, ${state.photoCount} 照片")
            }}
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontSize = 12.sp, letterSpacing = 0.5.sp, color = InkMuted, fontFamily = SansFont, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp))
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun FilterChipRow(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Rosewood.copy(alpha = 0.15f), selectedLabelColor = Rosewood),
                shape = RoundedCornerShape(12.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = InkMuted); Text(value, fontSize = 13.sp, color = Ink)
    }
}
