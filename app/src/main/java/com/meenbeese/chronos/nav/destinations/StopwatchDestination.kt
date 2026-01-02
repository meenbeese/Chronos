package com.meenbeese.chronos.nav.destinations

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

import com.meenbeese.chronos.R
import com.meenbeese.chronos.services.StopwatchService
import com.meenbeese.chronos.ui.screens.StopwatchScreen
import com.meenbeese.chronos.ui.views.LapItemView
import com.meenbeese.chronos.ui.views.ProgressTextView
import com.meenbeese.chronos.utils.FormatUtils

data class LapData(val number: Int, val lapTime: Long, val totalTime: Long)

@Composable
fun StopwatchDestination(navController: NavController) {
    val context = LocalContext.current

    var service by remember { mutableStateOf<StopwatchService?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var timeText by remember { mutableStateOf("0s 00") }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var maxProgress by remember { mutableFloatStateOf(0f) }
    var referenceProgress by remember { mutableStateOf<Float?>(null) }
    var laps by remember { mutableStateOf(listOf<LapData>()) }
    var showReset by remember { mutableStateOf(false) }
    var showLap by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }

    val progressModifier = if (LocalConfiguration.current.screenWidthDp > 600) {
        Modifier.fillMaxWidth(0.5f)
    } else {
        Modifier.fillMaxWidth()
    }

    val listener = remember {
        object : StopwatchService.Listener {
            override fun onStateChanged(running: Boolean) {
                isRunning = running
                if (running) {
                    showReset = false
                    showLap = true
                    showShare = false
                } else {
                    showLap = false
                    service?.let {
                        showReset = it.elapsedTime > 0
                        showShare = it.elapsedTime > 0
                    }
                }
            }

            override fun onReset() {
                laps = emptyList()
                maxProgress = 0f
                referenceProgress = 0f
                timeText = "0s 00"
                currentProgress = 0f
                showReset = false
                showLap = false
                showShare = false
            }

            override fun onTick(currentTime: Long, text: String) {
                timeText = text
                service?.let {
                    val lapBase = if (it.lastLapTime == 0L) currentTime else it.lastLapTime
                    currentProgress = (currentTime - lapBase).toFloat()
                }
            }

            override fun onLap(lapNum: Int, lapTime: Long, lastLapTime: Long, lapDiff: Long) {
                if (lastLapTime == 0L) {
                    maxProgress = lapDiff.toFloat()
                } else {
                    referenceProgress = lapDiff.toFloat()
                }

                laps = laps + LapData(
                    number = lapNum,
                    lapTime = lapDiff,
                    totalTime = lapTime
                )
            }
        }
    }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val localBinder = binder as? StopwatchService.LocalBinder
                service = localBinder?.service
                service?.setListener(listener)
                listener.onStateChanged(service?.isRunning ?: false)
                listener.onTick(0, "0s 00")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
            }
        }
    }

    DisposableEffect(Unit) {
        val intent = Intent(context, StopwatchService::class.java)
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            service?.setListener(null)
            context.unbindService(connection)
        }
    }

    StopwatchScreen(
        isRunning = isRunning,
        onBackClick = { navController.popBackStack() },
        onResetClick = { service?.reset() },
        onToggleClick = { service?.toggle() },
        onLapClick = { service?.lap() },
        onShareClick = {
            service?.let {
                val time = FormatUtils.formatMillis(it.elapsedTime)
                val content = buildString {
                    append(context.getString(R.string.title_time, time)).append("\n")
                    var total = 0L
                    for ((i, lap) in it.laps!!.withIndex()) {
                        total += lap
                        append(context.getString(R.string.title_lap_number, it.laps!!.size - i))
                            .append("    \t")
                            .append(context.getString(R.string.title_lap_time, FormatUtils.formatMillis(lap)))
                            .append("    \t")
                            .append(context.getString(R.string.title_total_time, FormatUtils.formatMillis(total))
                            )
                        if (i < it.laps!!.size - 1) append("\n")
                    }
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        context.getString(
                            R.string.title_stopwatch_share,
                            context.getString(R.string.app_name),
                            time
                        )
                    )
                    putExtra(Intent.EXTRA_TEXT, content)
                }
                context.startActivity(
                    Intent.createChooser(
                        shareIntent,
                        context.getString(R.string.title_share_results)
                    )
                )
            }
        },
        isResetVisible = showReset,
        isLapVisible = showLap,
        isShareVisible = showShare,
        timeContent = {
            ProgressTextView(
                text = timeText,
                progress = currentProgress,
                maxProgress = maxProgress,
                referenceProgress = referenceProgress,
                animate = true,
                modifier = progressModifier.aspectRatio(1f)
            )
        },
        lapsContent = {
            for (lap in laps.reversed()) {
                LapItemView(lap = lap)
            }
        }
    )
}
