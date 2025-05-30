package com.meenbeese.chronos.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.databinding.FragmentTimerBinding

class TimerFragment : BaseFragment() {
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isRunning = true
    private var timer: TimerData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        timer = arguments?.getParcelable(EXTRA_TIMER)

        timer?.duration?.let { binding.time.setMaxProgress(it) }

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    timer?.let { timer ->
                        if (timer.isSet) {
                            val remainingMillis = timer.remainingMillis
                            binding.time.apply {
                                setText(FormatUtils.formatMillis(remainingMillis))
                                setProgress(timer.duration - remainingMillis)
                            }
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
