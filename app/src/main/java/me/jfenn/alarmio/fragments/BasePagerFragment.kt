package me.jfenn.alarmio.fragments

import android.content.Context


abstract class BasePagerFragment : BaseFragment() {
    abstract fun getTitle(context: Context?): String?
}
