package com.meenbeese.chronos.interfaces

import android.content.Context


abstract class ContextFragmentInstantiator(private val context: Context) : FragmentInstantiator {

    override fun getTitle(position: Int): String? {
        return getTitle(context, position)
    }

    abstract fun getTitle(context: Context?, position: Int): String?
}
