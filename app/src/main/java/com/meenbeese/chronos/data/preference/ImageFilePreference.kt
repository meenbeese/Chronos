package com.meenbeese.chronos.data.preference

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

import com.meenbeese.chronos.data.PreferenceEntry
import com.meenbeese.chronos.ui.screens.FileChooserScreen
import com.meenbeese.chronos.ui.screens.FileChooserType
import com.meenbeese.chronos.ui.views.PreferenceItem

/**
 * A preference item that allows the user to select
 * an image from a file (the resulting preference
 * contains a valid image path / URI).
 */
@Composable
fun ImageFilePreference(
    preference: PreferenceEntry.StringPref,
    @StringRes title: Int,
    @StringRes description: Int,
    modifier: Modifier = Modifier
) {
    var showChooser by remember { mutableStateOf(false) }

    PreferenceItem(
        title = stringResource(id = title),
        description = stringResource(id = description),
        onClick = {
            showChooser = true
        },
        modifier = modifier
    )

    if (showChooser) {
        FileChooserScreen(
            type = FileChooserType.IMAGE,
            preference = preference,
            onFileChosen = { _, _ -> showChooser = false },
            onDismiss = { showChooser = false }
        )
    }
}
