package com.example.english_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary            = NavyPrimary,
    onPrimary          = TextOnPrimary,
    primaryContainer   = CardBg,
    onPrimaryContainer = NavyDark,
    secondary          = OrangeAccent,
    onSecondary        = TextOnPrimary,
    secondaryContainer = OrangeLight,
    onSecondaryContainer = OrangeDark,
    background         = BgLight,
    onBackground       = TextPrimary,
    surface            = SurfaceWhite,
    onSurface          = TextPrimary,
    surfaceVariant     = CardBg,
    onSurfaceVariant   = TextSecondary,
    error              = ErrorRed,
    onError            = TextOnPrimary,
)

@Composable
fun English_AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}