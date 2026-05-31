package com.keepsake.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════
// Muji Editorial Design Tokens — Exact Web Parity
// ═══════════════════════════════════════════════════════════════

// --- Core palette (public for screen references) ---
val Ink          = Color(0xFF2F3E2E)  // Primary text / icons
val Paper        = Color(0xFFF1EDE6)  // Background
val Rosewood     = Color(0xFF9F4E5A)  // Accent: buttons, FAB, focus rings
val CardBg       = Color(0xFFFAFAF8)  // Card surface
val InkMuted     = Color(0x802F3E2E)  // ~50% opacity ink for muted text
val InkFaint     = Color(0x332F3E2E)  // ~20% opacity ink for very faint
val InkVeryFaint = Color(0x142F3E2E)  // ~8% opacity for backgrounds
private val InkDark      = Color(0xFFC5D1C0)
private val PaperDark    = Color(0xFF1A1D19)
private val RosewoodDark = Color(0xFFE4A0A8)
private val CardDark     = Color(0xFF1E211D)

// --- Border tokens ---
val BorderSubtle = Color(0x142F3E2E)  // rgba(47,62,46,0.08)
private val BorderDefaultLight = Color(0x2E2F3E2E)
private val BorderSubtleDark   = Color(0x14C8D2C3)
private val BorderDefaultDark  = Color(0x2EC8D2C3)

// Typography
val SerifFont = FontFamily.Serif
val SansFont = FontFamily.Default

@Composable
fun outlinedFieldColors(focusedBorderColor: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = BorderSubtle,
    focusedLabelColor = focusedBorderColor,
    unfocusedLabelColor = InkMuted,
    cursorColor = focusedBorderColor
)

// ═══════════════════════════════════════════════════════════════
// Light Color Scheme
// ═══════════════════════════════════════════════════════════════

private val LightColorScheme = lightColorScheme(
    // Accent (rosewood) maps to M3 primary — used for FAB, buttons, active states
    primary             = Rosewood,
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFFFFDADF),
    onPrimaryContainer  = Color(0xFF3B0D15),

    // Secondary: muted sage variant of ink, for less-prominent UI
    secondary           = Color(0xFF58634E),
    onSecondary         = Color(0xFFFFFFFF),
    secondaryContainer  = Color(0xFFDCE8CF),
    onSecondaryContainer = Color(0xFF161F12),

    // Tertiary: warm olive-gray
    tertiary            = Color(0xFF6B705C),
    onTertiary          = Color(0xFFFFFFFF),
    tertiaryContainer   = Color(0xFFF2F5E6),
    onTertiaryContainer = Color(0xFF1E2317),

    // Error
    error               = Color(0xFFBA1A1A),
    onError             = Color(0xFFFFFFFF),
    errorContainer      = Color(0xFFFFDAD6),
    onErrorContainer    = Color(0xFF410002),

    // Background & Surface
    background          = Paper,
    onBackground        = Ink,
    surface             = Paper,
    onSurface           = Ink,
    surfaceVariant      = CardBg,
    onSurfaceVariant    = Color(0xFF434A3F),

    // Borders
    outline             = BorderDefaultLight,
    outlineVariant      = BorderSubtle,

    // Surface containers (elevation ramp, light)
    surfaceContainerLowest  = Color(0xFFFFFBF7),
    surfaceContainerLow     = Color(0xFFF7F3ED),
    surfaceContainer        = Paper,
    surfaceContainerHigh    = Color(0xFFEBE7E0),
    surfaceContainerHighest = Color(0xFFE5E1DA),

    // Scrim
    scrim = Color(0xFF000000),
)

// ═══════════════════════════════════════════════════════════════
// Dark Color Scheme
// ═══════════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    // Accent (rosewood) — slightly lighter
    primary             = RosewoodDark,
    onPrimary           = Color(0xFF3B0D15),
    primaryContainer    = Color(0xFF5E2933),
    onPrimaryContainer  = Color(0xFFFFDADF),

    // Secondary
    secondary           = Color(0xFFBCC2AC),
    onSecondary         = Color(0xFF232B20),
    secondaryContainer  = Color(0xFF3A4135),
    onSecondaryContainer = Color(0xFFD4DCCB),

    // Tertiary
    tertiary            = Color(0xFFA8AE9C),
    onTertiary          = Color(0xFF1E2317),
    tertiaryContainer   = Color(0xFF3B4034),
    onTertiaryContainer = Color(0xFFD0D6C4),

    // Error
    error               = Color(0xFFFFB4AB),
    onError             = Color(0xFF690005),
    errorContainer      = Color(0xFF93000A),
    onErrorContainer    = Color(0xFFFFDAD6),

    // Background & Surface
    background          = PaperDark,
    onBackground        = InkDark,
    surface             = PaperDark,
    onSurface           = InkDark,
    surfaceVariant      = CardDark,
    onSurfaceVariant    = Color(0xFFC0C7BA),

    // Borders
    outline             = BorderDefaultDark,
    outlineVariant      = BorderSubtleDark,

    // Surface containers (elevation ramp, dark)
    surfaceContainerLowest  = Color(0xFF141613),
    surfaceContainerLow     = PaperDark,
    surfaceContainer        = CardDark,
    surfaceContainerHigh    = Color(0xFF222520),
    surfaceContainerHighest = Color(0xFF272A24),

    // Scrim
    scrim = Color(0xFF000000),
)

// ═══════════════════════════════════════════════════════════════
// Typography — Muji editorial
// ═══════════════════════════════════════════════════════════════

private val AppTypography = Typography(
    // Display — serif for brand moments
    displayLarge  = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 45.sp),
    displaySmall  = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 36.sp),

    // Headlines — serif (page titles, room names)
    // headlineMedium = 24sp bold → exact match for "page titles"
    headlineLarge  = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    headlineSmall  = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp),

    // Titles — sans-serif
    titleLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,  fontSize = 16.sp),
    titleSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,  fontSize = 14.sp),

    // Body — system-ui sans-serif
    // bodyMedium = 14sp → exact match for "body"
    bodyLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp),

    // Labels — sans-serif
    // labelMedium = 12sp + letterSpacing 0.5 → exact match for "small labels / uppercase"
    labelLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.5.sp),
    labelSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.5.sp),
)

// ═══════════════════════════════════════════════════════════════
// Shapes — 12 dp default radius
// ═══════════════════════════════════════════════════════════════

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

// ═══════════════════════════════════════════════════════════════
// Theme
// ═══════════════════════════════════════════════════════════════

@Composable
fun KeepsakeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
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
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content,
    )
}

// ═══════════════════════════════════════════════════════════════
// Paper grain / noise texture utility
// ═══════════════════════════════════════════════════════════════

/**
 * Applies a subtle noise grain overlay to simulate the Muji paper
 * texture at ~2.5 % opacity. Uses a pseudo-random point pattern seeded
 * from the modifier bounds so the grain is stable across recompositions
 * and mimics the web version's CSS noise filter.
 */
@Composable
fun Modifier.paperGrain(): Modifier {
    val seed = remember { Random.nextInt() }
    return this.then(
        Modifier.drawWithContent {
            drawContent()
            // Draw ~200 tiny specks at 2.5 % opacity
            val rng = Random(seed)
            val specks = 200
            val alpha = 0.025f
            val dark = Color.Black.copy(alpha = alpha)
            val light = Color.White.copy(alpha = alpha)
            val w = size.width
            val h = size.height
            for (i in 0 until specks) {
                val x = rng.nextFloat() * w
                val y = rng.nextFloat() * h
                // Alternate black / white specks to simulate film grain
                val color = if (i % 2 == 0) dark else light
                drawCircle(color, radius = 1.5f, center = Offset(x, y))
            }
        }
    )
}