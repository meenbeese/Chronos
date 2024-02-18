package com.meenbeese.chronos.fragments

import android.os.Bundle

import androidx.fragment.app.Fragment

import com.meenbeese.chronos.Alarmio
import com.meenbeese.chronos.Alarmio.AlarmioListener


abstract class BaseFragment : Fragment(), AlarmioListener {
    protected var alarmio: Alarmio? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmio = context?.applicationContext as Alarmio
        alarmio?.addListener(this)
    }

    override fun onDestroy() {
        alarmio?.removeListener(this)
        alarmio = null
        super.onDestroy()
    }

    override fun onAlarmsChanged() {
        // Update any alarm-dependent data.
    }

    override fun onTimersChanged() {
        // Update any timer-dependent data.
    }
}
