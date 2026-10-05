package com.changedue.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = TextWhite,
    tertiary = ReceivedAccent,
    background = Bg,
    onBackground = TextWhite,
    surface = Card,
    onSurface = TextWhite,
    onSurfaceVariant = Muted,
    error = Red,
    secondary = Green
)

@Composable
fun ChangeDueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
