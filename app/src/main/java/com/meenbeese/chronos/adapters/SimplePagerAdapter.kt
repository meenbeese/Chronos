package com.meenbeese.chronos.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.meenbeese.chronos.interfaces.FragmentInstantiator


class SimplePagerAdapter(
    fragment: Fragment,
    private vararg val fragments: FragmentInstantiator
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position].newInstance(position)!!
    }

    fun getTitle(position: Int): String {
        return fragments[position].getTitle(position) ?: ""
    }
}
