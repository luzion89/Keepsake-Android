package com.keepsake.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@Composable
fun SwipeToDismissCard(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val dismissThreshold = 200f

    Box(modifier = Modifier.fillMaxWidth()) {
        // Red background (delete indicator)
        if (offsetX < 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFD32F2F), MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, "删除", tint = Color.White)
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = offsetX == 0f,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .offset(x = offsetX.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX.absoluteValue > dismissThreshold) {
                                    onDelete()
                                }
                                offsetX = 0f
                            }
                        ) { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-300f, 0f)
                        }
                    }
            ) {
                content()
            }
        }
    }
}
