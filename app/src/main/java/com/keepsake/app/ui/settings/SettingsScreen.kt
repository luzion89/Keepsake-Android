package com.keepsake.app.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showApiKey by remember { mutableStateOf(false) }
    var showDeepseekKey by remember { mutableStateOf(false) }
    var modelOverride by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设置") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("主题", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("light" to "浅色", "dark" to "深色", "system" to "跟随系统").forEach { (value, label) ->
                                FilterChip(
                                    selected = uiState.theme == value,
                                    onClick = { viewModel.setTheme(value) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // Language
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("语言 / Language", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("zh" to "中文", "en" to "English").forEach { (value, label) ->
                                FilterChip(
                                    selected = uiState.language == value,
                                    onClick = { viewModel.setLanguage(value) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // AI Config
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("AI 助手", style = MaterialTheme.typography.titleSmall)
                            Switch(checked = uiState.aiConfig.enabled, onCheckedChange = { viewModel.setAiEnabled(it) })
                        }

                        if (uiState.aiConfig.enabled) {
                            Spacer(Modifier.height(12.dp))

                            Text("Provider", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("deepseek" to "DeepSeek", "openrouter" to "OpenRouter").forEach { (v, l) ->
                                    FilterChip(selected = uiState.aiConfig.provider == v, onClick = { viewModel.setAiProvider(v) }, label = { Text(l) })
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = if (showDeepseekKey) (uiState.aiConfig.deepseekApiKey) else "●●●●●●●●",
                                onValueChange = { viewModel.setAiDeepseekKey(it) },
                                label = { Text("API Key") },
                                singleLine = true,
                                visualTransformation = if (showDeepseekKey) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = { IconButton(onClick = { showDeepseekKey = !showDeepseekKey }) { Icon(if (showDeepseekKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = modelOverride,
                                onValueChange = { modelOverride = it; viewModel.setAiModel(it) },
                                label = { Text("模型名称（可选）") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Test button
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val apiKey = uiState.aiConfig.apiKey
                                    if (apiKey.isNotBlank()) {
                                        viewModel.testApiConnection(uiState.aiConfig.provider, apiKey)
                                    }
                                },
                                enabled = !uiState.isTestingApi
                            ) {
                                if (uiState.isTestingApi) {
                                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("测试连接")
                                }
                            }

                            uiState.apiTestResult?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall, color = if (it.startsWith("连接成功")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Device & Data
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("本地数据", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Text("设备 ID: ${uiState.deviceId}", style = MaterialTheme.typography.bodySmall)
                        Text("房间: ${uiState.roomCount} | 区域: ${uiState.areaCount} | 物品: ${uiState.itemCount} | 照片: ${uiState.photoCount}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
