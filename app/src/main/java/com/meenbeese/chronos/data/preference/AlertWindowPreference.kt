package com.meenbeese.chronos.data.preference

import android.content.Context
import android.provider.Settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.openOverlaySettings
import com.meenbeese.chronos.dialogs.BackgroundPermissionsDialog

/**
 * A preference item allowing the user to grant the
 * overlay permissions / alert window to ignore background
 * restrictions when starting an activity.
 *
 * @see https://developer.android.com/guide/components/activities/background-starts
 */
@Composable
fun AlertWindowPreference(
    context: Context,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val isGranted = remember { Settings.canDrawOverlays(context) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (!isGranted) {
                    showDialog = true
                } else {
                    openOverlaySettings(context)
                }
            }
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.info_background_permissions_title),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Switch(
            checked = isGranted,
            onCheckedChange = null,
            enabled = false
        )
    }

    if (showDialog) {
        BackgroundPermissionsDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                openOverlaySettings(context)
            }
        )
    }
}
