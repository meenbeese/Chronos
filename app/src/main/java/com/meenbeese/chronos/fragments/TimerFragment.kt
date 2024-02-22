package com.meenbeese.chronos.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.views.ProgressTextView
import com.afollestad.aesthetic.Aesthetic.Companion.get
import com.google.android.material.floatingactionbutton.FloatingActionButton

import io.reactivex.disposables.Disposable


class TimerFragment : BaseFragment() {
    private var back: ImageView? = null
    private var time: ProgressTextView? = null
    private var stop: FloatingActionButton? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var isRunning = true
    private var timer: TimerData? = null
    private var textColorPrimarySubscription: Disposable? = null
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
        time?.setMaxProgress(timer!!.duration)
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    if (timer!!.isSet) {
                        val remainingMillis = timer!!.remainingMillis
                        time?.setText(FormatUtils.formatMillis(remainingMillis))
                        time?.setProgress(timer!!.duration - remainingMillis)
                        handler?.postDelayed(this, 10)
                    } else {
                        try {
                            val manager = fragmentManager
                            manager?.popBackStack()
                        } catch (e: IllegalStateException) {
                            handler?.postDelayed(this, 100)
                        }
                    }
                }
            }
        }
        stop?.setOnClickListener {
            chronos?.removeTimer(timer)
            fragmentManager?.popBackStack()
        }
        back?.setOnClickListener { fragmentManager?.popBackStack() }
        handler?.post(runnable as Runnable)
        textColorPrimarySubscription = get()
            .textColorPrimary()
            .subscribe { integer: Int? -> back?.setColorFilter(integer!!) }
        return view
    }

    override fun onDestroyView() {
        isRunning = false
        textColorPrimarySubscription?.dispose()
        time?.unsubscribe()
        super.onDestroyView()
    }

    companion object {
        const val EXTRA_TIMER = "meenbeese.chronos.TimerFragment.EXTRA_TIMER"
    }
}
