package com.meenbeese.chronos.nav.destinations

import android.os.Bundle

import androidx.fragment.app.Fragment

import com.meenbeese.chronos.Chronos

abstract class BaseFragment : Fragment() {
    protected var chronos: Chronos? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chronos = context?.applicationContext as Chronos
    }

    override fun onDestroy() {
        chronos = null
        super.onDestroy()
    }
}
