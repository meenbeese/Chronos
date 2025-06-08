package com.meenbeese.chronos.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.DialogFragment
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.SimplePagerAdapter
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.databinding.DialogSoundChooserBinding
import com.meenbeese.chronos.fragments.sound.AlarmSoundChooserFragment
import com.meenbeese.chronos.fragments.sound.FileSoundChooserFragment
import com.meenbeese.chronos.fragments.sound.RingtoneSoundChooserFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.meenbeese.chronos.utils.AudioUtils
import com.google.android.material.tabs.TabLayoutMediator

import org.koin.android.ext.android.inject

@UnstableApi
class SoundChooserDialog : DialogFragment(), SoundChooserListener {
    private val audioUtils: AudioUtils by inject()
    private var listener: SoundChooserListener? = null

    private var _binding: DialogSoundChooserBinding? = null
    private val binding get() = _binding!!

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
        _binding = DialogSoundChooserBinding.inflate(inflater, container, false)

        val fragments = arrayOf(
            AlarmSoundChooserFragment.Instantiator(context, this),
            RingtoneSoundChooserFragment.Instantiator(context, this),
            FileSoundChooserFragment.Instantiator(context, this)
        )

        binding.viewPager.adapter = SimplePagerAdapter(this, *fragments)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragments[position].getTitle(position)
        }.attach()

        return binding.root
    }

    fun setListener(listener: SoundChooserListener?) {
        this.listener = listener
    }

    override fun onSoundChosen(sound: SoundData?) {
        listener?.onSoundChosen(sound)
        dismiss()
    }

    @UnstableApi
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        audioUtils.stopCurrentSound()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
