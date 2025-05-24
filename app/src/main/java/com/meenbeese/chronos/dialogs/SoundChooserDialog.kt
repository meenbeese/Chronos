package com.meenbeese.chronos.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.SimplePagerAdapter
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.fragments.sound.AlarmSoundChooserFragment
import com.meenbeese.chronos.fragments.sound.FileSoundChooserFragment
import com.meenbeese.chronos.fragments.sound.RingtoneSoundChooserFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SoundChooserDialog : DialogFragment(), SoundChooserListener {
    private var listener: SoundChooserListener? = null
    private var view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.let { params ->
            params.windowAnimations = R.style.SlideDialogAnimation
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.dialog_sound_chooser, container, false)

        val tabLayout = view?.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = view?.findViewById<ViewPager2>(R.id.viewPager)

        val fragments = arrayOf(
            AlarmSoundChooserFragment.Instantiator(view?.context, this),
            RingtoneSoundChooserFragment.Instantiator(view?.context, this),
            FileSoundChooserFragment.Instantiator(view?.context, this)
        )

        viewPager?.adapter = SimplePagerAdapter(this, *fragments)

        tabLayout?.let { tl ->
            viewPager?.let { vp ->
                TabLayoutMediator(tl, vp) { tab, position ->
                    tab.text = fragments[position].getTitle(position)
                }.attach()
            }
        }

        return view
    }

    fun setListener(listener: SoundChooserListener?) {
        this.listener = listener
    }

    override fun onSoundChosen(sound: SoundData?) {
        listener?.onSoundChosen(sound)
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (view?.context?.applicationContext as Chronos).stopCurrentSound()
    }
}
