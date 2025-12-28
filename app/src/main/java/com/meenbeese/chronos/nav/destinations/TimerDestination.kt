package com.meenbeese.chronos.nav.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController

import com.meenbeese.chronos.db.TimerAlarmRepository
import com.meenbeese.chronos.ui.screens.TimerScreen
import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.delay

import org.koin.compose.koinInject

@Composable
fun TimerDestination(
    navController: NavController,
    timerId: Int
) {
    val repo: TimerAlarmRepository = koinInject()

    val timer = remember(timerId) {
        repo.timers.getOrNull(timerId)
    } ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var timeText by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    var maxProgress by remember { mutableFloatStateOf(timer.duration.toFloat()) }

    LaunchedEffect(timer) {
        while (true) {
            if (timer.isSet) {
                val remainingMillis = timer.remainingMillis
                timeText = FormatUtils.formatMillis(remainingMillis)
                progress = (timer.duration - remainingMillis).toFloat()
                delay(10)
            } else {
                navController.popBackStack()
                break
            }
        }
    }

    TimerScreen(
        timerText = timeText,
        progress = progress,
        maxProgress = maxProgress,
        onBack = { navController.popBackStack() },
        onStop = { repo.removeTimer(timer) }
    )
}
