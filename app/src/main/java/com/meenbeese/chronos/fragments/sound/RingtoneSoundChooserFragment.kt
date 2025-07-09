package com.meenbeese.chronos.fragments.sound

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.ext.loadRingtones
import com.meenbeese.chronos.fragments.BasePagerFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.meenbeese.chronos.views.SoundItemView

@UnstableApi
class RingtoneSoundChooserFragment : BaseSoundChooserFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val ringtones = loadRingtones(requireContext())
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp)
                ) {
                    items(ringtones) { sound ->
                        SoundItemView(
                            icon = painterResource(id = R.drawable.ic_play),
                            title = sound.name,
                            modifier = Modifier
                                .clickable { onSoundChosen(sound) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    override fun getTitle(context: Context?): String? {
        return context?.getString(R.string.title_ringtones)
    }

    internal class Instantiator(context: Context?, listener: SoundChooserListener?) :
        BaseSoundChooserFragment.Instantiator(context, listener) {
        override fun newInstance(
            position: Int,
            listener: SoundChooserListener?
        ): BasePagerFragment {
            val fragment: BaseSoundChooserFragment = RingtoneSoundChooserFragment()
            fragment.setListener(listener)
            return fragment
        }

        override fun getTitle(context: Context?, position: Int): String? {
            return context?.getString(R.string.title_ringtones)
        }
    }
}
