package com.meenbeese.chronos.nav.destinations

import android.app.AlarmManager
import android.content.Context
import android.widget.Toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController

import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.data.toEntity
import com.meenbeese.chronos.db.AlarmRepository
import com.meenbeese.chronos.db.AlarmViewModel
import com.meenbeese.chronos.db.AlarmViewModelFactory
import com.meenbeese.chronos.db.TimerAlarmRepository
import com.meenbeese.chronos.services.TimerService
import com.meenbeese.chronos.ui.screens.HomeScreen
import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.koin.compose.koinInject

import java.util.Calendar
import java.util.Date

@UnstableApi
@Composable
fun HomeDestination(
    navController: NavController,
    context: Context,
    intentAction: String?,
    navigateToStopwatch: () -> Unit,
    navigateToTimer: (timer: TimerData) -> Unit,
) {
    val repo: TimerAlarmRepository = koinInject()
    val alarmRepo: AlarmRepository = koinInject()
    val alarmViewModel: AlarmViewModel = viewModel(
        factory = AlarmViewModelFactory(alarmRepo)
    )

    val alarms by alarmViewModel.alarms.observeAsState(emptyList())
    val isBottomSheetExpanded = remember { mutableStateOf(false) }

    val nearestAlarmId = remember { mutableStateOf(getNearestAlarmId(repo)) }

    LaunchedEffect(alarms) {
        nearestAlarmId.value = getNearestAlarmId(repo)
    }

    HomeScreen(
        navController = navController,
        alarms = alarms,
        isBottomSheetExpanded = isBottomSheetExpanded,
        nearestAlarmId = nearestAlarmId,
        onAlarmUpdated = { alarmData ->
            CoroutineScope(Dispatchers.IO).launch {
                alarmViewModel.update(alarmData.toEntity())
            }
        },
        onAlarmDeleted = { alarmData ->
            CoroutineScope(Dispatchers.IO).launch {
                alarmViewModel.delete(alarmData.toEntity())
            }
        },
        onScheduleAlarm = { h, m ->
            scheduleAlarm(context, alarmViewModel, h, m)
        },
        onScheduleWatch = { navigateToStopwatch() },
        onScheduleTimer = { h, m, s, ring, vibrate ->
            scheduleTimer(context, repo, h, m, s, ring, vibrate, navigateToTimer)
        },
        navigateToNearestAlarm = {
            nearestAlarmId.value = getNearestAlarmId(repo)
        },
        intentAction = intentAction
    )
}

/**
 * Open the alarm scheduler dialog to allow the user to create
 * a new alarm.
 */
private fun scheduleAlarm(
    context: Context,
    alarmViewModel: AlarmViewModel,
    hour: Int,
    minute: Int
) {
    val time = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (BuildConfig.DEBUG) add(Calendar.MINUTE, 1)
    }.timeInMillis

    val alarm = AlarmData(
        id = 0,
        name = null,
        time = Calendar.getInstance().apply { timeInMillis = time },
        isEnabled = true,
        days = MutableList(7) { false },
        isVibrate = true,
        sound = null
    )

    CoroutineScope(Dispatchers.IO).launch {
        val entity = alarm.toEntity()
        val id = alarmViewModel.insertAndReturnId(entity)
        alarm.id = id.toInt()
        alarm.set(context)
    }

    val formatted = FormatUtils.formatShort(context, Date(time))
    Toast.makeText(context, "Alarm set for $formatted", Toast.LENGTH_SHORT).show()
}

/**
 * Open the timer scheduler dialog to allow the user to start
 * a timer.
 */
private fun scheduleTimer(
    context: Context,
    repo: TimerAlarmRepository,
    hours: Int,
    minutes: Int,
    seconds: Int,
    ringtone: SoundData?,
    isVibrate: Boolean,
    navigateToTimer: (TimerData) -> Unit
) {
    val totalMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L
    if (totalMillis <= 0) {
        Toast.makeText(context, "Invalid timer duration", Toast.LENGTH_SHORT).show()
        return
    }

    val timer = repo.newTimer()
    timer.setDuration(totalMillis, context)
    timer.setVibrate(context, isVibrate)
    timer.setSound(context, ringtone)
    timer[context] = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    TimerService.Companion.startService(context)

    navigateToTimer(timer)
}

private fun getNearestAlarmId(repo: TimerAlarmRepository): Int {
    val allAlarms = repo.alarms

    val alarmsWithNextTrigger = allAlarms
        .filter { it.isEnabled }
        .mapNotNull { alarm -> alarm.getNext()?.timeInMillis?.let { alarm to it } }

    val targetAlarm = alarmsWithNextTrigger
        .minByOrNull { it.second }?.first
        ?: allAlarms
            .mapNotNull { alarm -> alarm.getNext()?.timeInMillis?.let { alarm to it } }
            .minByOrNull { it.second }
            ?.first

    return targetAlarm?.id ?: -1
}
