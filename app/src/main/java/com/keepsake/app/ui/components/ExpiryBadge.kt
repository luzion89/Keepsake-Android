package com.keepsake.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

private val EXPIRY_WARN = TimeUnit.DAYS.toMillis(30)
private val EXPIRY_DANGER = TimeUnit.DAYS.toMillis(7)

@Composable
fun ExpiryBadge(expiresAt: Long?, modifier: Modifier = Modifier) {
    if (expiresAt == null) return

    val now = System.currentTimeMillis()
    val remaining = expiresAt - now
    val daysLeft = TimeUnit.MILLISECONDS.toDays(remaining)

    val (text, bgColor, textColor) = when {
        remaining <= 0 -> "已过期" to Color(0xFFFFEBEE) to Color(0xFFB71C1C)
        remaining <= EXPIRY_DANGER -> "还有${daysLeft}天" to Color(0xFFFFF3E0) to Color(0xFFE65100)
        remaining <= EXPIRY_WARN -> "还有${daysLeft}天" to Color(0xFFF1F8E9) to Color(0xFF33691E)
        else -> return
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
