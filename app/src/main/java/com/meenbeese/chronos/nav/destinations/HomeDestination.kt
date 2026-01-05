package com.meenbeese.chronos.nav.destinations

import android.content.Context
import android.widget.Toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController

import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.db.TimerAlarmRepository
import com.meenbeese.chronos.ui.screens.HomeScreen
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.viewmodel.HomeViewModel
import com.meenbeese.chronos.viewmodel.HomeViewModelFactory

import org.koin.compose.koinInject

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
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repo)
    )

    val alarms by homeViewModel.alarms.observeAsState(emptyList())
    val timers by homeViewModel.timers.observeAsState(emptyList())
    val isBottomSheetExpanded = remember { mutableStateOf(false) }

    HomeScreen(
        navController = navController,
        alarms = alarms,
        timers = timers,
        isBottomSheetExpanded = isBottomSheetExpanded,
        onAlarmUpdated = { alarmData ->
            homeViewModel.updateAlarm(context, alarmData)
        },
        onAlarmDeleted = { alarmData ->
            homeViewModel.deleteAlarm(context, alarmData)
        },
        onScheduleAlarm = { h, m ->
            homeViewModel.scheduleAlarm(context, h, m) { triggerTime ->
                val formatted = FormatUtils.formatShort(context, Date(triggerTime))
                Toast.makeText(context, "Alarm set for $formatted", Toast.LENGTH_SHORT).show()
            }
        },
        onScheduleWatch = { navigateToStopwatch() },
        onScheduleTimer = { h, m, s, ring, vibrate ->
            homeViewModel.scheduleTimer(
                context,
                h, m, s,
                ring,
                vibrate,
                onTimerStarted = navigateToTimer,
                onInvalidDuration = {
                    Toast.makeText(context, "Invalid timer duration", Toast.LENGTH_SHORT).show()
                }
            )
        },
        intentAction = intentAction
    )
}
