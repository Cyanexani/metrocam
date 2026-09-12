package com.metrocam.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MetroColors = darkColorScheme(
    primary = MetroLime,
    onPrimary = MetroBlack,
    background = MetroBlack,
    onBackground = Color.White,
    surface = MetroPanel,
    onSurface = Color.White
)

@Composable
fun MetrocamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MetroColors,
        typography = Typography,
        content = content
    )
}
