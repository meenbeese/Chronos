package com.meenbeese.chronos.ui.screens

import android.content.Context

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.ext.isIgnoringBatteryOptimizations
import com.meenbeese.chronos.data.preference.AboutPreference
import com.meenbeese.chronos.data.preference.AlertWindowPreference
import com.meenbeese.chronos.data.preference.BooleanPreference
import com.meenbeese.chronos.data.preference.ColorSchemePreference
import com.meenbeese.chronos.data.preference.ImportExportPreference
import com.meenbeese.chronos.data.preference.RadioPreference
import com.meenbeese.chronos.data.preference.RingtonePreference
import com.meenbeese.chronos.data.preference.ThemePreference
import com.meenbeese.chronos.data.preference.TimePreference
import com.meenbeese.chronos.ext.handleBatteryOptimizationClick
import com.meenbeese.chronos.ui.views.PreferenceItem

import kotlinx.coroutines.CoroutineScope

import kotlin.time.Duration.Companion.minutes

data class PreferenceBlock(
    val visible: Boolean = true,
    val content: @Composable () -> Unit
)

@UnstableApi
@Composable
fun SettingsScreen(
    navController: NavController,
    context: Context,
    lifecycleScope: CoroutineScope = rememberCoroutineScope()
) {
    val batteryOptimizationNeeded = remember {
        !isIgnoringBatteryOptimizations(context)
    }

    val preferenceList = remember {
        listOf(
            PreferenceBlock {
                ImportExportPreference()
            },
            PreferenceBlock(visible = batteryOptimizationNeeded) {
                PreferenceItem(
                    title = stringResource(R.string.title_ignore_battery_optimizations),
                    onClick = { handleBatteryOptimizationClick(context) }
                )
            },
            PreferenceBlock {
                AlertWindowPreference(context)
            },
            PreferenceBlock {
                ThemePreference(coroutineScope = lifecycleScope)
            },
            PreferenceBlock {
                ColorSchemePreference(
                    nameRes = R.string.color_scheme,
                    onSelectionChanged = {}
                )
            },
            PreferenceBlock {
                BooleanPreference(
                    preference = Preferences.RINGING_BACKGROUND_IMAGE,
                    title = R.string.title_ringing_background_image,
                    description = R.string.desc_ringing_background_image
                )
            },
            PreferenceBlock {
                BooleanPreference(
                    preference = Preferences.SCROLL_TO_NEXT,
                    title = R.string.title_scroll_next,
                    description = R.string.desc_scroll_next
                )
            },
            PreferenceBlock {
                RingtonePreference(
                    preference = Preferences.DEFAULT_ALARM_RINGTONE,
                    titleRes = R.string.title_default_alarm_ringtone
                )
            },
            PreferenceBlock {
                RingtonePreference(
                    preference = Preferences.DEFAULT_TIMER_RINGTONE,
                    titleRes = R.string.title_default_timer_ringtone
                )
            },
            PreferenceBlock {
                BooleanPreference(
                    preference = Preferences.PLAY_ON_HEADPHONES,
                    title = R.string.title_play_headphones,
                    description = R.string.desc_play_headphones
                )
            },
            PreferenceBlock {
                BooleanPreference(
                    preference = Preferences.SLEEP_REMINDER,
                    title = R.string.title_sleep_reminder,
                    description = R.string.desc_sleep_reminder
                )
            },
            PreferenceBlock {
                TimePreference(
                    preference = Preferences.SLEEP_REMINDER_TIME,
                    titleRes = R.string.title_sleep_reminder_time
                )
            },
            PreferenceBlock {
                BooleanPreference(
                    preference = Preferences.SLOW_WAKE_UP,
                    title = R.string.title_slow_wake_up,
                    description = R.string.desc_slow_wake_up
                )
            },
            PreferenceBlock {
                RadioPreference(
                    preference = Preferences.SLOW_WAKE_UP_TIME,
                    titleRes = R.string.title_slow_wake_up_time,
                    options = listOf(
                        1.minutes,
                        2.minutes,
                        5.minutes,
                        10.minutes,
                        15.minutes,
                        30.minutes
                    )
                )
            },
            PreferenceBlock {
                AboutPreference(navController = navController)
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(preferenceList.filter { it.visible }) { item ->
            item.content()
        }
    }
}
