package com.meenbeese.chronos.nav.destinations

import android.os.Bundle

import androidx.fragment.app.Fragment

import com.meenbeese.chronos.Chronos

abstract class BaseFragment : Fragment(), Chronos.ChronosListener {
    protected var chronos: Chronos? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chronos = context?.applicationContext as Chronos
        chronos?.addListener(this)
    }

    override fun onDestroy() {
        chronos?.removeListener(this)
        chronos = null
        super.onDestroy()
    }

    override fun onAlarmsChanged() {
        // Update any alarm-dependent data.
    }

    override fun onTimersChanged() {
        // Update any timer-dependent data.
    }
}
