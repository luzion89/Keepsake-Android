package com.keepsake.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Web-style partial swipe: short left swipe (>=40dp) reveals an 88dp red delete zone.
 * Short right swipe (>=10dp) cancels the reveal.
 * Tapping the red zone triggers onDelete. Tapping elsewhere cancels.
 */
@Composable
fun SwipeToRevealCard(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var swiped by remember { mutableStateOf(false) }
    val offset by animateDpAsState(if (swiped) (-88).dp else 0.dp, tween(200), label = "swipe")

    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Red delete background
        Box(
            Modifier.matchParentSize().background(Color(0xFFD32F2F), RoundedCornerShape(12.dp)).padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("删除", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Delete, "删除", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // Swipeable front layer
        Box(
            Modifier
                .offset(x = offset)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount < -40f) swiped = true
                        else if (dragAmount > 10f) swiped = false
                    }
                }
                .then(
                    if (swiped) Modifier.clickable { swiped = false }
                    else Modifier
                )
        ) {
            content()
        }

        // Invisible delete tap target when revealed
        if (swiped) {
            Box(
                Modifier.align(Alignment.CenterEnd).width(88.dp).fillMaxHeight().clickable {
                    swiped = false
                    onDelete()
                }
            )
        }
    }
}
