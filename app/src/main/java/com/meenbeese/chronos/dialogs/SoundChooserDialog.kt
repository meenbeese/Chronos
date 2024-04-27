package com.meenbeese.chronos.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.SimplePagerAdapter
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.fragments.sound.AlarmSoundChooserFragment
import com.meenbeese.chronos.fragments.sound.FileSoundChooserFragment
import com.meenbeese.chronos.fragments.sound.RingtoneSoundChooserFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.afollestad.aesthetic.Aesthetic.Companion.get
import com.google.android.material.tabs.TabLayout


class SoundChooserDialog : DialogFragment(), SoundChooserListener {
    private var listener: SoundChooserListener? = null
    private var view: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
        get()
            .colorPrimary()
            .take(1)
            .subscribe { integer: Int ->
                view?.setBackgroundColor(integer)
            }
        val tabLayout = view?.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = view?.findViewById<ViewPager>(R.id.viewPager)
        val alarmFragment = AlarmSoundChooserFragment()
        val ringtoneFragment = RingtoneSoundChooserFragment()
        val fileFragment = FileSoundChooserFragment()
        alarmFragment.setListener(this)
        ringtoneFragment.setListener(this)
        fileFragment.setListener(this)
        viewPager?.adapter = SimplePagerAdapter(
            context, childFragmentManager,
            AlarmSoundChooserFragment.Instantiator(view?.context, this),
            RingtoneSoundChooserFragment.Instantiator(view?.context, this),
            FileSoundChooserFragment.Instantiator(view?.context, this)
        )
        tabLayout?.setupWithViewPager(viewPager)
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
