package com.meenbeese.chronos.adapters

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

import com.meenbeese.chronos.fragments.BasePagerFragment
import com.meenbeese.chronos.interfaces.FragmentInstantiator


class SimplePagerAdapter(
    fragMan: FragmentManager,
    private vararg val fragments: FragmentInstantiator
) : FragmentStatePagerAdapter(fragMan, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

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
