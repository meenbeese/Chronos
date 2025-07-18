package com.meenbeese.chronos.ui.screens

import android.content.Context

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.ext.isIgnoringBatteryOptimizations
import com.meenbeese.chronos.data.preference.AboutPreference
import com.meenbeese.chronos.data.preference.AlertWindowPreference
import com.meenbeese.chronos.data.preference.BatteryPreference
import com.meenbeese.chronos.data.preference.BooleanPreference
import com.meenbeese.chronos.data.preference.ColorPreference
import com.meenbeese.chronos.data.preference.ColorSchemePreference
import com.meenbeese.chronos.data.preference.ImageFilePreference
import com.meenbeese.chronos.data.preference.ImportExportPreference
import com.meenbeese.chronos.data.preference.RingtonePreference
import com.meenbeese.chronos.data.preference.SegmentedPreference
import com.meenbeese.chronos.data.preference.ThemePreference
import com.meenbeese.chronos.data.preference.TimePreference
import com.meenbeese.chronos.data.preference.TimeZonesPreference

import kotlinx.coroutines.CoroutineScope

@ExperimentalMaterial3Api
@UnstableApi
@Composable
fun SettingsScreen(
    context: Context,
    chronos: Chronos,
    lifecycleScope: CoroutineScope = rememberCoroutineScope()
) {
    var triggerRebuild by remember { mutableIntStateOf(0) }

    val batteryOptimizationNeeded = remember {
        !isIgnoringBatteryOptimizations(context)
    }

    val preferenceList = remember(triggerRebuild) {
        mutableStateListOf<@Composable () -> Unit>().apply {
            add { ImportExportPreference() }

            if (batteryOptimizationNeeded) {
                add { BatteryPreference(context) }
            }

            add { AlertWindowPreference(context) }
            add {
                ThemePreference(
                    chronos = chronos,
                    coroutineScope = lifecycleScope
                )
            }

            add {
                ColorSchemePreference(
                    nameRes = R.string.color_scheme,
                    onSelectionChanged = {

                    }
                )
            }

            add {
                SegmentedPreference(
                    nameRes = R.string.title_background_mode,
                    onSelectionChanged = {
                        triggerRebuild++
                    }
                )
            }

            // Add background detail (color or image)
            val isColor = Preferences.COLORFUL_BACKGROUND.get(context)
            add(
                if (isColor) {
                    {
                        ColorPreference(
                            preference = Preferences.BACKGROUND_COLOR,
                            title = R.string.title_background_color
                        )
                    }
                } else {
                    {
                        ImageFilePreference(
                            preference = Preferences.BACKGROUND_IMAGE,
                            title = R.string.title_background_image,
                            description = R.string.desc_background_image
                        )
                    }
                }
            )

            addAll(
                listOf(
                    {
                        BooleanPreference(
                            preference = Preferences.RINGING_BACKGROUND_IMAGE,
                            title = R.string.title_ringing_background_image,
                            description = R.string.desc_ringing_background_image
                        )
                    },
                    {
                        BooleanPreference(
                            preference = Preferences.SCROLL_TO_NEXT,
                            title = R.string.title_scroll_next,
                            description = R.string.desc_scroll_next
                        )
                    },
                    {
                        TimeZonesPreference(
                            onTimeZonesChanged = {}
                        )
                    },
                    {
                        BooleanPreference(
                            preference = Preferences.MILITARY_TIME,
                            title = R.string.title_military_time
                        )
                    },
                    {
                        RingtonePreference(
                            preference = Preferences.DEFAULT_ALARM_RINGTONE,
                            titleRes = R.string.title_default_alarm_ringtone
                        )
                    },
                    {
                        RingtonePreference(
                            preference = Preferences.DEFAULT_TIMER_RINGTONE,
                            titleRes = R.string.title_default_timer_ringtone
                        )
                    },
                    {
                        BooleanPreference(
                            preference = Preferences.SLEEP_REMINDER,
                            title = R.string.title_sleep_reminder,
                            description = R.string.desc_sleep_reminder
                        )
                    },
                    {
                        TimePreference(
                            preference = Preferences.SLEEP_REMINDER_TIME,
                            titleRes = R.string.title_sleep_reminder_time
                        )
                    },
                    {
                        BooleanPreference(
                            preference = Preferences.SLOW_WAKE_UP,
                            title = R.string.title_slow_wake_up,
                            description = R.string.desc_slow_wake_up
                        )
                    },
                    {
                        TimePreference(
                            preference = Preferences.SLOW_WAKE_UP_TIME,
                            titleRes = R.string.title_slow_wake_up_time
                        )
                    },
                    {
                        AboutPreference(
                            context = context
                        )
                    }
                )
            )
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(preferenceList) { item ->
            item()
        }
    }
}
