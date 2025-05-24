package com.meenbeese.chronos.fragments.sound

import android.content.Context

import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.fragments.BasePagerFragment
import com.meenbeese.chronos.interfaces.ContextFragmentInstantiator
import com.meenbeese.chronos.interfaces.SoundChooserListener

abstract class BaseSoundChooserFragment : BasePagerFragment(), SoundChooserListener {
    private var listener: SoundChooserListener? = null
    fun setListener(listener: SoundChooserListener?) {
        this.listener = listener
    }

    override fun onSoundChosen(sound: SoundData?) {
        listener?.onSoundChosen(sound)
    }

    override fun onDestroy() {
        super.onDestroy()
        listener = null
    }

    internal abstract class Instantiator(
        context: Context?,
        private val listener: SoundChooserListener?
    ) : ContextFragmentInstantiator(context!!) {

        override fun newInstance(position: Int): BasePagerFragment? {
            return listener?.let { newInstance(position, it) }
        }

        abstract fun newInstance(position: Int, listener: SoundChooserListener?): BasePagerFragment?
    }
}
