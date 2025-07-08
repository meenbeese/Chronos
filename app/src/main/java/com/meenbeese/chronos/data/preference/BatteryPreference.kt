package com.meenbeese.chronos.data.preference

import android.content.Context

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.handleBatteryOptimizationClick
import com.meenbeese.chronos.views.PreferenceItem

/**
 * A preference item allowing the user to select to
 * ignore battery optimizations to improve stability.
 */
@Composable
fun BatteryPreference(
    context: Context,
    modifier: Modifier = Modifier
) {
    PreferenceItem(
        title = stringResource(R.string.title_ignore_battery_optimizations),
        onClick = {
            handleBatteryOptimizationClick(context)
        },
        modifier = modifier
    )
}
