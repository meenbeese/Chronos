package me.jfenn.alarmio.interfaces

import me.jfenn.alarmio.fragments.BasePagerFragment


interface FragmentInstantiator {
    fun newInstance(position: Int): BasePagerFragment?
    fun getTitle(position: Int): String?
}
