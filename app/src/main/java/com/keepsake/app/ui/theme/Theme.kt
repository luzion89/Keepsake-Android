package com.keepsake.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Material Design 3 颜色方案 — 暖色调中性风格
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5C6B4E),         // 鼠尾草绿
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDEE8D2),
    onPrimaryContainer = Color(0xFF192310),
    secondary = Color(0xFF8B5E3C),       // 暖棕色
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDCC3),
    onSecondaryContainer = Color(0xFF301A05),
    tertiary = Color(0xFF4A6B7A),        // 蓝灰
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFBF9F5),      // 暖白
    onBackground = Color(0xFF1C1B1A),
    surface = Color(0xFFFBF9F5),
    onSurface = Color(0xFF1C1B1A),
    surfaceVariant = Color(0xFFF0EDE7),
    onSurfaceVariant = Color(0xFF4A4640),
    error = Color(0xFFBA1A1A),
    surfaceContainerLowest = Color(0xFFFFFBF7),
    surfaceContainerLow = Color(0xFFF5F2EC),
    surfaceContainer = Color(0xFFEFECE6),
    surfaceContainerHigh = Color(0xFFE9E6E1),
    surfaceContainerHighest = Color(0xFFE3E0DB)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC2CCB7),
    onPrimary = Color(0xFF2D3822),
    primaryContainer = Color(0xFF445338),
    onPrimaryContainer = Color(0xFFDEE8D2),
    secondary = Color(0xFFF0BD93),
    onSecondary = Color(0xFF4B2B12),
    secondaryContainer = Color(0xFF654127),
    onSecondaryContainer = Color(0xFFFFDCC3),
    tertiary = Color(0xFFB8D1E0),
    onTertiary = Color(0xFF1F3B49),
    background = Color(0xFF131311),
    onBackground = Color(0xFFE4E2DD),
    surface = Color(0xFF131311),
    onSurface = Color(0xFFE4E2DD),
    surfaceVariant = Color(0xFF4A4640),
    onSurfaceVariant = Color(0xFFCDC6BB),
    error = Color(0xFFFFB4AB)
)

@Composable
fun KeepsakeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surfaceContainerHighest.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
