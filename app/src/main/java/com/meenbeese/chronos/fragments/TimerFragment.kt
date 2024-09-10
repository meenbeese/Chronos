package com.meenbeese.chronos.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.views.ProgressTextView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class TimerFragment : BaseFragment() {
    private lateinit var back: ImageView
    private lateinit var time: ProgressTextView
    private lateinit var stop: FloatingActionButton
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isRunning = true
    private var timer: TimerData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)
        back = view.findViewById(R.id.back)
        time = view.findViewById(R.id.time)
        stop = view.findViewById(R.id.stop)
        timer = arguments?.getParcelable(EXTRA_TIMER)
        timer?.duration?.let { time.setMaxProgress(it) }
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    timer?.let { timer ->
                        if (timer.isSet) {
                            val remainingMillis = timer.remainingMillis
                            time.apply {
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
        stop.setOnClickListener {
            timer?.let { it1 -> chronos?.removeTimer(it1) }
            parentFragmentManager.popBackStack()
        }
        back.setOnClickListener { parentFragmentManager.popBackStack() }
        handler.post(runnable)
        return view
    }

    override fun onDestroyView() {
        isRunning = false
        super.onDestroyView()
    }

    companion object {
        const val EXTRA_TIMER = "meenbeese.chronos.TimerFragment.EXTRA_TIMER"
    }
}
