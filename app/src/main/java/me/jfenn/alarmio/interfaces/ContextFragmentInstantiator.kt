package me.jfenn.alarmio.interfaces

import android.content.Context

import java.lang.ref.WeakReference


abstract class ContextFragmentInstantiator(context: Context) : FragmentInstantiator {
    private val context: WeakReference<Context>

    init {
        this.context = WeakReference(context)
    }

    override fun getTitle(position: Int): String? {
        val context = context.get()
        return context?.let { getTitle(it, position) }
    }

    abstract fun getTitle(context: Context?, position: Int): String?
}
