package com.meenbeese.chronos.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.R
import com.meenbeese.chronos.screens.StopwatchScreen
import com.meenbeese.chronos.services.StopwatchService
import com.meenbeese.chronos.utils.FormatUtils.formatMillis
import com.meenbeese.chronos.views.ProgressTextView

class StopwatchFragment : BaseFragment(), StopwatchService.Listener, ServiceConnection {
    private var service: StopwatchService? = null

    private var timeText by mutableStateOf("0s 00")
    private var currentProgress by mutableFloatStateOf(0f)
    private var maxProgress by mutableFloatStateOf(0f)
    private var referenceProgress by mutableStateOf<Float?>(null)

    private var isRunning by mutableStateOf(false)
    private var laps by mutableStateOf(listOf<LapData>())
    private var showReset by mutableStateOf(false)
    private var showLap by mutableStateOf(false)
    private var showShare by mutableStateOf(false)

    data class LapData(val number: Int, val lapTime: Long, val totalTime: Long)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = requireContext()
        val intent = Intent(context, StopwatchService::class.java)
        context.startService(intent)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)

        return ComposeView(context).apply {
            setContent {
                StopwatchScreen(
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onResetClick = { service?.reset() },
                    onToggleClick = { service?.toggle() },
                    onLapClick = { service?.lap() },
                    onShareClick = {
                        service?.let {
                            val time = formatMillis(it.elapsedTime)
                            val content = buildString {
                                append(getString(R.string.title_time, time)).append("\n")
                                var total = 0L
                                for ((i, lap) in it.laps!!.withIndex()) {
                                    total += lap
                                    append(getString(R.string.title_lap_number, it.laps!!.size - i))
                                        .append("    \t")
                                        .append(getString(R.string.title_lap_time, formatMillis(lap)))
                                        .append("    \t")
                                        .append(getString(R.string.title_total_time, formatMillis(total)))
                                    if (i < it.laps!!.size - 1) append("\n")
                                }
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.title_stopwatch_share, getString(R.string.app_name), time))
                                putExtra(Intent.EXTRA_TEXT, content)
                            }
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.title_share_results)))
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    },
                    lapsContent = {
                        for (lap in laps.reversed()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(
                                    text = getString(R.string.title_lap_number, lap.number),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = getString(R.string.title_lap_time, formatMillis(lap.lapTime)),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = getString(R.string.title_total_time, formatMillis(lap.totalTime)),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onDestroyView() {
        service?.let {
            it.setListener(null)
            val wasRunning = it.isRunning
            context?.unbindService(this)
            if (!wasRunning) {
                context?.stopService(Intent(context, StopwatchService::class.java))
            }
        }
        super.onDestroyView()
    }

    override fun onStateChanged(isRunning: Boolean) {
        this.isRunning = isRunning
        if (isRunning) {
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
        service?.let {
            timeText = text
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

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        if (iBinder is StopwatchService.LocalBinder) {
            service = iBinder.service
            service?.isRunning?.let { onStateChanged(it) }
            onTick(0, "0s 00")
            service?.setListener(this)
        }
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        service = null
    }
}
