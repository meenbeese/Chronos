package com.meenbeese.chronos.ui.screens

import android.provider.AlarmClock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.db.AlarmEntity
import com.meenbeese.chronos.ext.getFlow
import com.meenbeese.chronos.ui.dialogs.AlarmSchedulerDialog
import com.meenbeese.chronos.ui.dialogs.TimerSchedulerDialog
import com.meenbeese.chronos.ui.views.AnimatedFabMenu
import com.meenbeese.chronos.ui.views.ClockPageView
import com.meenbeese.chronos.ui.views.FabItem
import com.meenbeese.chronos.ui.views.HomeBottomSheet
import com.meenbeese.chronos.utils.ImageUtils

import kotlinx.datetime.TimeZone

@Composable
fun HomeScreen(
    navController: NavController,
    alarms: List<AlarmEntity>,
    isBottomSheetExpanded: MutableState<Boolean>,
    nearestAlarmId: MutableState<Int>,
    onAlarmUpdated: (AlarmData) -> Unit,
    onAlarmDeleted: (AlarmData) -> Unit,
    onScheduleAlarm: (hour: Int, minute: Int) -> Unit,
    onScheduleWatch: () -> Unit,
    onScheduleTimer: (h: Int, m: Int, s: Int, ring: SoundData?, vibrate: Boolean) -> Unit,
    navigateToNearestAlarm: () -> Unit,
    intentAction: String?,
    modifier: Modifier = Modifier
) {
    val homeTabs = listOf("Alarms", "Settings")
    val selectedTabIndex = remember { mutableIntStateOf(0) }

    var showAlarmDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    val clockBackground = ImageUtils.rememberBackgroundPainterState(isAlarm = false)

    val context = LocalContext.current
    val timeZoneEnabled by Preferences.TIME_ZONE_ENABLED.getFlow(context).collectAsState(initial = false)
    val selectedZonesCsv by Preferences.TIME_ZONES.getFlow(context).collectAsState(initial = "")

    /*
     * Check actions passed from MainActivity; open timer/alarm
     * schedulers if necessary.
     */
    LaunchedEffect(intentAction) {
        when (intentAction) {
            AlarmClock.ACTION_SET_ALARM -> showAlarmDialog = true
            AlarmClock.ACTION_SET_TIMER -> showTimerDialog = true
        }
    }

    /*
     * Observe alarms using LiveData and update the list and
     * UI when an alarm is updated or deleted.
     */
    LaunchedEffect(alarms) {
        if (alarms.isEmpty() && !isBottomSheetExpanded.value) {
            isBottomSheetExpanded.value = true
        }
    }

    val selectedZones = buildList {
        add(TimeZone.currentSystemDefault().id)
        if (timeZoneEnabled) {
            selectedZonesCsv
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && TimeZone.availableZoneIds.contains(it) }
                .forEach { add(it) }
        }
    }

    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600

    if (isTablet) {
        Row(modifier = modifier.fillMaxSize()) {
            clockBackground?.let { painter ->
                ClockPageView(
                    timeZones = selectedZones,
                    backgroundPainter = painter,
                    pageIndicatorVisible = selectedZones.size > 1,
                    navigateToNearestAlarm = navigateToNearestAlarm,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                HomeBottomSheet(
                    tabs = homeTabs,
                    isTablet = true,
                    initialTabIndex = selectedTabIndex.intValue,
                    onTabChanged = { selectedTabIndex.intValue = it },
                    heightFraction = 0.5f
                ) { page ->
                    if (page == 0) {
                        AlarmsScreen(
                            alarms = alarms,
                            onAlarmUpdated = onAlarmUpdated,
                            onAlarmDeleted = onAlarmDeleted,
                            nearestAlarmId = nearestAlarmId.value,
                            isBottomSheetExpanded = isBottomSheetExpanded
                        )
                    } else {
                        SettingsScreen(
                            navController = navController,
                            context = context
                        )
                    }
                }
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            clockBackground?.let { painter ->
                ClockPageView(
                    timeZones = selectedZones,
                    backgroundPainter = painter,
                    pageIndicatorVisible = selectedZones.size > 1,
                    navigateToNearestAlarm = navigateToNearestAlarm,
                    modifier = Modifier.fillMaxHeight(0.5f + 0.05f)
                )
            }

            HomeBottomSheet(
                tabs = homeTabs,
                isTablet = false,
                initialTabIndex = selectedTabIndex.intValue,
                onTabChanged = { selectedTabIndex.intValue = it },
                heightFraction = 0.5f
            ) { page ->
                if (page == 0) {
                    AlarmsScreen(
                        alarms = alarms,
                        onAlarmUpdated = onAlarmUpdated,
                        onAlarmDeleted = onAlarmDeleted,
                        nearestAlarmId = nearestAlarmId.value,
                        isBottomSheetExpanded = isBottomSheetExpanded
                    )
                } else {
                    SettingsScreen(
                        navController = navController,
                        context = context
                    )
                }
            }
        }
    }

    if (selectedTabIndex.intValue == 0) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            AnimatedFabMenu(
                icon = R.drawable.ic_add,
                text = R.string.title_create,
                items = listOf(
                    FabItem(R.drawable.ic_timer, R.string.title_set_timer),
                    FabItem(R.drawable.ic_stopwatch, R.string.title_set_stopwatch),
                    FabItem(R.drawable.ic_alarm_add, R.string.title_set_alarm)
                ),
                onItemClick = { fabItem ->
                    when (fabItem.text) {
                        R.string.title_set_timer -> showTimerDialog = true
                        R.string.title_set_stopwatch -> onScheduleWatch()
                        R.string.title_set_alarm -> showAlarmDialog = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }

    if (showAlarmDialog) {
        AlarmSchedulerDialog(
            onDismiss = { showAlarmDialog = false },
            onTimeSet = { hour, minute ->
                showAlarmDialog = false
                onScheduleAlarm(hour, minute)
            }
        )
    }

    if (showTimerDialog) {
        TimerSchedulerDialog(
            onDismiss = { showTimerDialog = false },
            onTimeChosen = { h, m, s, ring, vibrate ->
                showTimerDialog = false
                onScheduleTimer(h, m, s, ring, vibrate)
            }
        )
    }
}
