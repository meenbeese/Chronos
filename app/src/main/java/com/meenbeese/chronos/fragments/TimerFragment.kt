package com.meenbeese.chronos.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.databinding.FragmentTimerBinding
import com.meenbeese.chronos.views.ProgressTextView

class TimerFragment : BaseFragment() {
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isRunning = true
    private var timer: TimerData? = null

    private var timeText by mutableStateOf("")
    private var progress by mutableStateOf(0f)
    private var maxProgress by mutableStateOf(0f)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        _binding!!.time.setContent {
            ProgressTextView(
                text = timeText,
                progress = progress,
                maxProgress = maxProgress,
                referenceProgress = null,
                animate = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
        timer = arguments?.getParcelable(EXTRA_TIMER)

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    timer?.let { timer ->
                        if (timer.isSet) {
                            val remainingMillis = timer.remainingMillis

                            timeText = FormatUtils.formatMillis(remainingMillis)
                            progress = (timer.duration - remainingMillis).toFloat()
                            maxProgress = timer.duration.toFloat()

                            handler.postDelayed(this, 10)
                        } else {
                            try {
                                parentFragmentManager.popBackStack()
                            } catch (e: IllegalStateException) {
                                handler.postDelayed(this, 100)
                            }
                        }
                    }
                }
            }
        }

        binding.stop.setOnClickListener {
            timer?.let { chronos?.removeTimer(it) }
            parentFragmentManager.popBackStack()
        }

        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        handler.post(runnable)

        return binding.root
    }

    override fun onDestroyView() {
        isRunning = false
        handler.removeCallbacks(runnable)
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val EXTRA_TIMER = "meenbeese.chronos.TimerFragment.EXTRA_TIMER"
    }
}
