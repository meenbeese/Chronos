package com.meenbeese.chronos.interfaces

import com.meenbeese.chronos.fragments.BasePagerFragment

interface FragmentInstantiator {
    fun newInstance(position: Int): BasePagerFragment?
    fun getTitle(position: Int): String?
}
