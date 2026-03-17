package com.meenbeese.chronos.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.ext.isIgnoringBatteryOptimizations
import com.meenbeese.chronos.ui.preferences.AboutPreference
import com.meenbeese.chronos.ui.preferences.AlertWindowPreference
import com.meenbeese.chronos.ui.preferences.BooleanPreference
import com.meenbeese.chronos.ui.preferences.ColorSchemePreference
import com.meenbeese.chronos.ui.preferences.ImportExportPreference
import com.meenbeese.chronos.ui.preferences.RadioPreference
import com.meenbeese.chronos.ui.preferences.RingtonePreference
import com.meenbeese.chronos.ui.preferences.ThemePreference
import com.meenbeese.chronos.ui.preferences.TimePreference
import com.meenbeese.chronos.ext.handleBatteryOptimizationClick
import com.meenbeese.chronos.ui.views.PreferenceItem

import kotlin.time.Duration.Companion.minutes

data class PreferenceBlock(
    val id: String,
    val visible: Boolean = true,
    val content: @Composable () -> Unit
)

@UnstableApi
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var batteryOptimizationNeeded by remember {
        mutableStateOf(!isIgnoringBatteryOptimizations(context))
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                batteryOptimizationNeeded = !isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val preferenceList = listOf(
        PreferenceBlock(id = "import_export") {
            ImportExportPreference()
        },
        PreferenceBlock(id = "battery_optimizations", visible = batteryOptimizationNeeded) {
            PreferenceItem(
                title = stringResource(R.string.title_ignore_battery_optimizations),
                onClick = { handleBatteryOptimizationClick(context) }
            )
        },
        PreferenceBlock(id = "alert_window") {
            AlertWindowPreference(context)
        },
        PreferenceBlock(id = "theme") {
            ThemePreference(coroutineScope = lifecycleScope)
        },
        PreferenceBlock(id = "color_scheme") {
            ColorSchemePreference(
                nameRes = R.string.color_scheme,
                onSelectionChanged = {}
            )
        },
        PreferenceBlock(id = "ringing_background_image") {
            BooleanPreference(
                preference = Preferences.RINGING_BACKGROUND_IMAGE,
                title = R.string.title_ringing_background_image,
                description = R.string.desc_ringing_background_image
            )
        },
        PreferenceBlock(id = "scroll_to_next") {
            BooleanPreference(
                preference = Preferences.SCROLL_TO_NEXT,
                title = R.string.title_scroll_next,
                description = R.string.desc_scroll_next
            )
        },
        PreferenceBlock(id = "default_alarm_ringtone") {
            RingtonePreference(
                preference = Preferences.DEFAULT_ALARM_RINGTONE,
                titleRes = R.string.title_default_alarm_ringtone
            )
        },
        PreferenceBlock(id = "default_timer_ringtone") {
            RingtonePreference(
                preference = Preferences.DEFAULT_TIMER_RINGTONE,
                titleRes = R.string.title_default_timer_ringtone
            )
        },
        PreferenceBlock(id = "play_on_headphones") {
            BooleanPreference(
                preference = Preferences.PLAY_ON_HEADPHONES,
                title = R.string.title_play_headphones,
                description = R.string.desc_play_headphones
            )
        },
        PreferenceBlock(id = "sleep_reminder") {
            BooleanPreference(
                preference = Preferences.SLEEP_REMINDER,
                title = R.string.title_sleep_reminder,
                description = R.string.desc_sleep_reminder
            )
        },
        PreferenceBlock(id = "sleep_reminder_time") {
            TimePreference(
                preference = Preferences.SLEEP_REMINDER_TIME,
                titleRes = R.string.title_sleep_reminder_time
            )
        },
        PreferenceBlock(id = "slow_wake_up") {
            BooleanPreference(
                preference = Preferences.SLOW_WAKE_UP,
                title = R.string.title_slow_wake_up,
                description = R.string.desc_slow_wake_up
            )
        },
        PreferenceBlock(id = "slow_wake_up_time") {
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
        PreferenceBlock(id = "about") {
            AboutPreference(navController = navController)
        }
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = preferenceList.filter { it.visible },
            key = { it.id }
        ) { item ->
            item.content()
        }
    }
}
