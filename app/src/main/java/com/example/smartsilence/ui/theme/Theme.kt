package com.example.smartsilence.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SmartSilenceDarkColors = darkColorScheme(

    primary = ElectricBlue,

    onPrimary = Color.White,

    primaryContainer = SurfaceNavyLight,

    onPrimaryContainer = TextWhite,

    secondary = BrightBlue,

    onSecondary = Color.White,

    secondaryContainer = SurfaceNavy,

    onSecondaryContainer = TextPrimary,

    background = BackgroundNavy,

    onBackground = TextWhite,

    surface = SurfaceNavy,

    onSurface = TextWhite,

    surfaceVariant = SurfaceNavyLight,

    onSurfaceVariant = TextSecondary,

    outline = DividerBlue,

    error = ErrorRed,

    onError = Color.White
)

@Composable
fun SmartSilenceTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    val colorScheme = SmartSilenceDarkColors

    val view = LocalView.current

    if (!view.isInEditMode) {

        val window =
            (view.context as Activity).window

        WindowCompat
            .getInsetsController(
                window,
                view
            )
            .isAppearanceLightStatusBars = false
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}