package com.nutrisafe.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandMagenta,
    secondary = BrandOrange,
    background = Surface,
    surface = Card,
    onPrimary = Color.White,
    onSecondary = Ink,
    onBackground = Ink,
    onSurface = Ink,
    outline = Line
)

@Composable
fun NutriSafeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = NutriSafeTypography,
        content = content
    )
}
