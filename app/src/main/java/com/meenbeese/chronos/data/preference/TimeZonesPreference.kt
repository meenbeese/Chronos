package com.meenbeese.chronos.data.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.activities.MainActivity
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.dialogs.TimeZoneChooserDialog
import com.meenbeese.chronos.views.PreferenceItem

import kotlinx.coroutines.runBlocking

/**
 * A preference item allowing the user to select
 * from multiple time zones (preference is a boolean,
 * should have a parameter for the zone id).
 */
@UnstableApi
@Composable
fun TimeZonesPreference(
    modifier: Modifier = Modifier,
    onTimeZonesChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val selectedZonesCsv = remember {
        mutableStateOf(Preferences.TIME_ZONES.get(context))
    }

    val selectedCount = selectedZonesCsv.value
        .split(",")
        .count { it.isNotBlank() }

    val summary = stringResource(
        R.string.msg_time_zones_selected,
        selectedCount
    )

    PreferenceItem(
        title = stringResource(R.string.title_time_zones),
        description = summary,
        onClick = { showDialog = true },
        modifier = modifier
    )

    if (showDialog) {
        TimeZoneChooserDialog(
            initialSelected = selectedZonesCsv.value
                .split(",")
                .filter { it.isNotBlank() }
                .toMutableSet(),
            onDismiss = {
                showDialog = false
            },
            onSelectionDone = { updatedSelection ->
                val csv = updatedSelection.joinToString(",")
                runBlocking {
                    Preferences.TIME_ZONES.set(context, csv)
                    Preferences.TIME_ZONE_ENABLED.set(context, updatedSelection.isNotEmpty())
                }
                selectedZonesCsv.value = csv
                onTimeZonesChanged()
                showDialog = false
            }
        )
    }
}
