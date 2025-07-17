package com.meenbeese.chronos.data.preference

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.Battery0Bar
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.ui.theme.ThemeMode

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Allow the user to choose the theme of the
 * application.
 */
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
        mutableStateOf(ThemeMode.fromInt(currentThemeValue))
    }

    var selectedText by remember {
        mutableStateOf(themes.getOrNull(selectedTheme.value) ?: themes.first())
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.title_theme),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

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
                    .width(160.dp),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                themes.forEachIndexed { index, themeName ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = themeName,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Outlined.AutoMode    // AUTO
                                        1 -> Icons.Outlined.WbSunny     // DAY
                                        2 -> Icons.Outlined.DarkMode    // NIGHT
                                        3 -> Icons.Outlined.Battery0Bar // AMOLED
                                        else -> Icons.Outlined.Settings
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            selectedText = themeName
                            selectedTheme = ThemeMode.fromInt(index)
                            coroutineScope.launch {
                                chronos.activityTheme = selectedTheme
                                Preferences.THEME.set(context, selectedTheme.value)
                            }
                        }
                    )
                }
            }
        }
    }
}
