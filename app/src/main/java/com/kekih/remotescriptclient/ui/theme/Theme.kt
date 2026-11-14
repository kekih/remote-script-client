package com.kekih.remotescriptclient.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF0B0E14)
val SurfaceDark = Color(0xFF151A22)
val CardDark = Color(0xFF1C2330)
val Amber = Color(0xFFE7B04A)
val Cyan = Color(0xFF4FC3F7)
val TextPrimary = Color(0xFFE8EEF7)
val TextSecondary = Color(0xFF8B95A8)
val ErrorRed = Color(0xFFEF5350)
val SuccessGreen = Color(0xFF66BB6A)

private val DarkColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Color(0xFF001018),
    secondary = Amber,
    background = Ink,
    surface = SurfaceDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed,
    surfaceVariant = CardDark,
)

@Composable
fun VixHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
