package com.meenbeese.chronos.data.preference

import android.app.Activity
import android.content.Context

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.utils.Theme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Allow the user to choose the theme of the
 * application.
 */
@ExperimentalMaterial3Api
@Composable
fun ThemePreference(
    chronos: Chronos,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themes = stringArrayResource(id = R.array.array_themes)
    val currentThemeValue = remember { Preferences.THEME.get(context) }
    var expanded by remember { mutableStateOf(false) }

    var selectedTheme by remember {
        mutableStateOf(Theme.fromInt(currentThemeValue))
    }

    var selectedText by remember {
        mutableStateOf(themes.getOrNull(selectedTheme.value) ?: themes.first())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title_theme),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                readOnly = true,
                value = selectedText,
                onValueChange = {},
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                themes.forEachIndexed { index, themeName ->
                    DropdownMenuItem(
                        text = { Text(themeName) },
                        onClick = {
                            expanded = false
                            selectedText = themeName
                            selectedTheme = Theme.fromInt(index)
                            coroutineScope.launch {
                                applyAndSaveTheme(context, chronos, selectedTheme)
                                (context as? Activity)?.recreate()
                            }
                        }
                    )
                }
            }
        }
    }
}

suspend fun applyAndSaveTheme(context: Context, chronos: Chronos, theme: Theme) {
    chronos.activityTheme = theme
    Preferences.THEME.set(context, theme.value)
    when (theme) {
        Theme.AUTO   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        Theme.DAY    -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Theme.NIGHT,
        Theme.AMOLED -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
