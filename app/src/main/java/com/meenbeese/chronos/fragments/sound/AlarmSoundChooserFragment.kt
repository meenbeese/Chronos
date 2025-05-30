package com.meenbeese.chronos.fragments.sound

import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.LinearLayoutManager

import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.SoundsAdapter
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.databinding.FragmentSoundChooserListBinding
import com.meenbeese.chronos.fragments.BasePagerFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener

class AlarmSoundChooserFragment : BaseSoundChooserFragment() {
    private var _binding: FragmentSoundChooserListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSoundChooserListBinding.inflate(inflater, container, false)

        binding.recycler.layoutManager = LinearLayoutManager(context)

        val sounds = mutableListOf<SoundData>()
        val manager = RingtoneManager(context).apply {
            setType(RingtoneManager.TYPE_ALARM)
        }

        val cursor = manager.cursor
        val count = cursor.count
        if (count > 0 && cursor.moveToFirst()) {
            do {
                sounds.add(
                    SoundData(
                        cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX),
                        SoundData.TYPE_RINGTONE,
                        cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(
                            RingtoneManager.ID_COLUMN_INDEX
                        )
                    )
                )
            } while (cursor.moveToNext())
        }

        val adapter = SoundsAdapter(chronos!!, sounds)
        adapter.setListener(this)
        binding.recycler.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun getTitle(context: Context?): String? {
        return context?.getString(R.string.title_alarms)
    }

    internal class Instantiator(context: Context?, listener: SoundChooserListener?) :
        BaseSoundChooserFragment.Instantiator(context, listener) {
        override fun newInstance(
            position: Int,
            listener: SoundChooserListener?
        ): BasePagerFragment {
            val fragment: BaseSoundChooserFragment = AlarmSoundChooserFragment()
            fragment.setListener(listener)
            return fragment
        }

        override fun getTitle(context: Context?, position: Int): String? {
            return context?.getString(R.string.title_alarms)
        }
    }
}
