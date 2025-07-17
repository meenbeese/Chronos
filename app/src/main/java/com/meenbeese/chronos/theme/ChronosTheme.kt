package com.meenbeese.chronos.theme

import android.app.Activity
import android.os.Build

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import com.meenbeese.chronos.Chronos

@Composable
fun ChronosTheme(
    chronos: Chronos,
    customColorScheme: ColorScheme,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeValue = chronos.activityTheme
    val isDarkTheme = themeValue.isDark()

    val colorScheme = when {
        themeValue == ThemeMode.AMOLED -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(context).copy(background = Color.Black, surface = Color.Black)
            } else {
                customColorScheme.copy(background = Color.Black, surface = Color.Black)
            }
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> customColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            val insetsController = WindowCompat.getInsetsController(activity.window, view)

            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

/*
    ChronosTheme(
        chronos = context.applicationContext as Chronos,
        customColorScheme = ThemeFactory.getSchemeFromSeed(
            color = Preferences.COLOR_SEED.get(context),
            dark = (context.applicationContext as Chronos).activityTheme.isDark()
        ),
        dynamicColor = Preferences.DYNAMIC_COLOR.get(context),
    ) {
        SettingsScreen(
            context = requireContext(),
            chronos = requireContext().applicationContext as Chronos
        )
    }
*/
