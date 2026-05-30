package com.keepsake.app.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "确定",
    dismissLabel: String = "取消",
    isDangerous: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = if (isDangerous) ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ) else ButtonDefaults.textButtonColors()
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        }
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    message: String
) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
    ) {
        androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(80.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = androidx.compose.ui.Modifier.size(64.dp)
        )
        androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
