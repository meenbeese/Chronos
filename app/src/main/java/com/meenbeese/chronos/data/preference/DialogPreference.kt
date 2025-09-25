package com.meenbeese.chronos.data.preference

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

import com.meenbeese.chronos.ui.views.PreferenceItem

/**
 * A preference item allowing the user to select
 * from multiple time zones (preference is a boolean,
 * should have a parameter for the zone id).
 */
@Composable
fun DialogPreference(
    @StringRes title: Int,
    description: String? = null,
    modifier: Modifier = Modifier,
    dialog: @Composable (onDismiss: () -> Unit) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    PreferenceItem(
        title = stringResource(title),
        description = description,
        onClick = { showDialog = true },
        modifier = modifier
    )

    if (showDialog) {
        dialog { showDialog = false }
    }
}
