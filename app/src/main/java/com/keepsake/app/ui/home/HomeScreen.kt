package com.keepsake.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keepsake.app.domain.model.Room
import com.keepsake.app.ui.components.ConfirmDialog
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════════
// Web color parity — exact values from web design tokens
// ═══════════════════════════════════════════════════════════════
private val Accent = Color(0xFF9F4E5A)
private val CardBg = Color(0xFFFAFAF8)
private val DeleteRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRoomClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Room?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fabRotation by animateFloatAsState(
        targetValue = if (uiState.showBottomSheet) 45f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "fabRotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DoorFront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "房间",
                            style = MaterialTheme.typography.headlineMedium  // 24sp serif bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showBottomSheet() },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                containerColor = Accent,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加房间",
                    modifier = Modifier.graphicsLayer { rotationZ = fabRotation }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                // ── Loading ──
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Accent
                    )
                }

                // ── Empty state ──
                uiState.rooms.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.DoorFront,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "还没有房间",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ── Room list inside a single Card ──
                else -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column {
                            uiState.rooms.forEachIndexed { index, room ->
                                SwipeableRoomRow(
                                    room = room,
                                    isEditing = uiState.editingRoomId == room.id,
                                    editingName = uiState.editingName,
                                    onNameChange = { viewModel.updateEditingName(it) },
                                    onStartRename = { viewModel.startEditing(room) },
                                    onCommitRename = { viewModel.commitRename() },
                                    onCancelEdit = { viewModel.cancelEditing() },
                                    onClick = { onRoomClick(room.id) },
                                    onDelete = { deleteTarget = room }
                                )
                                if (index < uiState.rooms.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Bottom sheet — add room
    // ═══════════════════════════════════════════════════════════════
    if (uiState.showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissBottomSheet() },
            sheetState = sheetState,
            containerColor = CardBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 0.dp
        ) {
            AddRoomSheetContent(
                onDismiss = { viewModel.dismissBottomSheet() },
                onConfirm = { name -> viewModel.createRoom(name) }
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Delete confirmation dialog
    // ═══════════════════════════════════════════════════════════════
    deleteTarget?.let { room ->
        ConfirmDialog(
            title = "删除房间",
            message = "确认删除「${room.name}」？该房间下的所有区域和物品也会被删除。",
            confirmLabel = "删除",
            isDangerous = true,
            onConfirm = {
                viewModel.deleteRoom(room.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Swipeable row wrapper — red background with trash icon + 删除
// ═══════════════════════════════════════════════════════════════
@Composable
private fun SwipeableRoomRow(
    room: Room,
    isEditing: Boolean,
    editingName: String,
    onNameChange: (String) -> Unit,
    onStartRename: () -> Unit,
    onCommitRename: () -> Unit,
    onCancelEdit: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 180.dp.toPx() }

    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
        // ── Red delete background revealed on swipe ──
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(DeleteRed)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "删除",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Foreground row that slides ──
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .background(CardBg)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX.absoluteValue > dismissThresholdPx) {
                                onDelete()
                            }
                            offsetX = 0f
                        }
                    ) { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(
                            -(dismissThresholdPx * 1.5f),
                            0f
                        )
                    }
                }
        ) {
            RoomRowContent(
                room = room,
                isEditing = isEditing,
                editingName = editingName,
                onNameChange = onNameChange,
                onStartRename = onStartRename,
                onCommitRename = onCommitRename,
                onCancelEdit = onCancelEdit,
                onClick = onClick
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Single room row — door icon + name + area count + edit + chevron
// ═══════════════════════════════════════════════════════════════
@Composable
private fun RoomRowContent(
    room: Room,
    isEditing: Boolean,
    editingName: String,
    onNameChange: (String) -> Unit,
    onStartRename: () -> Unit,
    onCommitRename: () -> Unit,
    onCancelEdit: () -> Unit,
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    if (isEditing) {
        // ── Editing mode: TextField with accent border, 8dp radius, auto-focused ──
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DoorFront,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))

            OutlinedTextField(
                value = editingName,
                onValueChange = onNameChange,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Accent.copy(alpha = 0.6f),
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    cursorColor = Accent
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )

            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onCommitRename, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "确定",
                    tint = Accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onCancelEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "取消",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    } else {
        // ── Normal display mode ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DoorFront,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))

            // Room name + area count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        // Serif, 14sp, medium — titleSmall is 14sp Medium sans-serif
                        // We override font family to serif for the web parity
                        fontFamily = FontFamily.Serif
                    )
                )
                Text(
                    text = "${room.areaCount} 个区域",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Pencil icon → triggers inline rename
            IconButton(
                onClick = onStartRename,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "重命名",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Chevron right
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Bottom sheet content — name input + preset chips + confirm
// ═══════════════════════════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddRoomSheetContent(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val presets = listOf("厨房", "客厅", "卧室", "卫生间", "阳台", "储物间", "书房", "餐厅")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // ── Name input ──
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 60) name = it },
            label = { Text("房间名称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                focusedLabelColor = Accent,
                cursorColor = Accent,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(Modifier.height(16.dp))

        // ── Preset chips label ──
        Text(
            "预设名称",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(10.dp))

        // ── Preset chips: pill-shaped, rounded-full ──
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            presets.forEach { preset ->
                SuggestionChip(
                    onClick = { name = preset },
                    label = {
                        Text(
                            preset,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (name == preset) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(50),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (name == preset) Accent
                        else MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = if (name != preset) SuggestionChipDefaults.suggestionChipBorder(
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        enabled = true
                    ) else null
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Confirm button ──
        Button(
            onClick = { onConfirm(name.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = Color.White,
                disabledContainerColor = Accent.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "确定",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
