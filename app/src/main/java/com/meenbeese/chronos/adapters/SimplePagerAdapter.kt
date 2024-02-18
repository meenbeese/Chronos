package com.meenbeese.chronos.adapters

import android.content.Context

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

import com.meenbeese.chronos.fragments.BasePagerFragment
import com.meenbeese.chronos.interfaces.FragmentInstantiator


class SimplePagerAdapter(
    context: Context?,
    fm: FragmentManager?,
    vararg fragments: FragmentInstantiator
) : FragmentStatePagerAdapter(
    fm!!
) {
    private val fragments: Array<out FragmentInstantiator>

    init {
        this.fragments = fragments
    }

    override fun getItem(position: Int): BasePagerFragment {
        return fragments[position].newInstance(position)!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragments[position].getTitle(position)
    }

    override fun getCount(): Int {
        return fragments.size
    }
}
