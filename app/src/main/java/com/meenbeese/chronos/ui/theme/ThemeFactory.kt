package com.meenbeese.chronos.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

object ThemeFactory {
    fun getSchemeFromSeed(color: Int, dark: Boolean): ColorScheme {
        val seed = Color(color)

        return if (dark) {
            ColorScheme(
                primary = seed.copy(alpha = 1f),
                onPrimary = Color.White,
                primaryContainer = seed.copy(alpha = 0.8f),
                onPrimaryContainer = Color.Black,
                inversePrimary = seed.copy(alpha = 0.6f),
                secondary = Color.Gray,
                onSecondary = Color.White,
                secondaryContainer = Color.DarkGray,
                onSecondaryContainer = Color.White,
                tertiary = Color.LightGray,
                onTertiary = Color.Black,
                tertiaryContainer = Color.Gray,
                onTertiaryContainer = Color.White,
                background = Color.Black,
                onBackground = Color.White,
                surface = Color.Black,
                onSurface = Color.White,
                surfaceVariant = Color.DarkGray,
                onSurfaceVariant = Color.LightGray,
                surfaceTint = seed,
                inverseSurface = Color.White,
                inverseOnSurface = Color.Black,
                error = Color.Red,
                onError = Color.White,
                errorContainer = Color.Red.copy(alpha = 0.5f),
                onErrorContainer = Color.White,
                outline = Color.Gray,
                outlineVariant = Color.LightGray,
                scrim = Color.Black,
                surfaceBright = Color(0.15f, 0.15f, 0.15f),
                surfaceDim = Color(0.05f, 0.05f, 0.05f),
                surfaceContainer = Color(0.08f, 0.08f, 0.08f),
                surfaceContainerHigh = Color(0.1f, 0.1f, 0.1f),
                surfaceContainerHighest = Color(0.12f, 0.12f, 0.12f),
                surfaceContainerLow = Color(0.06f, 0.06f, 0.06f),
                surfaceContainerLowest = Color(0.04f, 0.04f, 0.04f)
            )
        } else {
            ColorScheme(
                primary = seed,
                onPrimary = Color.White,
                primaryContainer = seed.copy(alpha = 0.2f),
                onPrimaryContainer = Color.Black,
                inversePrimary = seed.copy(alpha = 0.4f),
                secondary = Color.Gray,
                onSecondary = Color.Black,
                secondaryContainer = Color.LightGray,
                onSecondaryContainer = Color.Black,
                tertiary = Color.Gray,
                onTertiary = Color.Black,
                tertiaryContainer = Color.LightGray,
                onTertiaryContainer = Color.Black,
                background = Color.White,
                onBackground = Color.Black,
                surface = Color.White,
                onSurface = Color.Black,
                surfaceVariant = Color.LightGray,
                onSurfaceVariant = Color.DarkGray,
                surfaceTint = seed,
                inverseSurface = Color.Black,
                inverseOnSurface = Color.White,
                error = Color.Red,
                onError = Color.White,
                errorContainer = Color.Red.copy(alpha = 0.5f),
                onErrorContainer = Color.White,
                outline = Color.Gray,
                outlineVariant = Color.DarkGray,
                scrim = Color.Black,
                surfaceBright = Color(0.98f, 0.98f, 0.98f),
                surfaceDim = Color(0.9f, 0.9f, 0.9f),
                surfaceContainer = Color(0.94f, 0.94f, 0.94f),
                surfaceContainerHigh = Color(0.96f, 0.96f, 0.96f),
                surfaceContainerHighest = Color(0.98f, 0.98f, 0.98f),
                surfaceContainerLow = Color(0.92f, 0.92f, 0.92f),
                surfaceContainerLowest = Color(0.9f, 0.9f, 0.9f)
            )
        }
    }

    val catpucchinLatte = arrayOf(
        Color(220, 138, 120),
        Color(221, 120, 120),
        Color(234, 118, 203),
        Color(136, 57, 239),
        Color(210, 15, 57),
        Color(230, 69, 83),
        Color(254, 100, 11),
        Color(223, 142, 29),
        Color(64, 160, 43),
        Color(23, 146, 153),
        Color(4, 165, 229),
        Color(32, 159, 181),
        Color(30, 102, 245),
        Color(114, 135, 253)
    )
}
