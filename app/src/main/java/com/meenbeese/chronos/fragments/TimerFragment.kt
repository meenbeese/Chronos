package com.meenbeese.chronos.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView

import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.screens.TimerScreen

class TimerFragment : BaseFragment() {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isRunning = true
    private var timer: TimerData? = null

    private var timeText by mutableStateOf("")
    private var progress by mutableFloatStateOf(0f)
    private var maxProgress by mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timer = arguments?.getParcelable(EXTRA_TIMER)
        timer?.duration?.let { maxProgress = it.toFloat() }

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    timer?.let { timer ->
                        if (timer.isSet) {
                            val remainingMillis = timer.remainingMillis
                            timeText = FormatUtils.formatMillis(remainingMillis)
                            progress = (timer.duration - remainingMillis).toFloat()
                            handler.postDelayed(this, 10)
                        } else {
                            try {
                                parentFragmentManager.popBackStack()
                            } catch (_: IllegalStateException) {
                                handler.postDelayed(this, 100)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        handler.post(runnable)

        return ComposeView(requireContext()).apply {
            setContent {
                TimerScreen(
                    timerText = timeText,
                    progress = progress,
                    maxProgress = maxProgress,
                    onBack = { parentFragmentManager.popBackStack() },
                    onStop = {
                        timer?.let { chronos?.removeTimer(it) }
                        parentFragmentManager.popBackStack()
                    }
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isRunning = false
        handler.removeCallbacks(runnable)
    }

    companion object {
        const val EXTRA_TIMER = "meenbeese.chronos.TimerFragment.EXTRA_TIMER"
    }
}
