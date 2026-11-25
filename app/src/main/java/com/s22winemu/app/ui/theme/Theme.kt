package com.s22winemu.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val S22BgDark = Color(0xFF0A0E17)
val S22Surface = Color(0xFF131A29)
val S22SurfaceVariant = Color(0xFF1C2538)
val S22Cyan = Color(0xFF00E5FF)
val S22Violet = Color(0xFF7C4DFF)
val S22Emerald = Color(0xFF00E676)
val S22Amber = Color(0xFFFFAB40)
val S22Red = Color(0xFFFF5252)

private val S22ColorScheme = darkColorScheme(
    primary = S22Cyan,
    onPrimary = Color(0xFF002026),
    secondary = S22Violet,
    onSecondary = Color.White,
    tertiary = S22Emerald,
    background = S22BgDark,
    onBackground = Color(0xFFE6ECF5),
    surface = S22Surface,
    onSurface = Color(0xFFE6ECF5),
    surfaceVariant = S22SurfaceVariant,
    onSurfaceVariant = Color(0xFFA7B4C9)
)

@Composable
fun S22WinEmuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = S22ColorScheme,
        content = content
    )
}
