package com.meenbeese.chronos.fragments.sound

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.ext.loadRingtones
import com.meenbeese.chronos.fragments.BasePagerFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.meenbeese.chronos.utils.AudioUtils
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
                val audioUtils = AudioUtils(requireContext())
                val ringtones = loadRingtones(requireContext())
                var currentPlayingUrl by remember { mutableStateOf<String?>(null) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp)
                ) {
                    items(ringtones) { sound ->
                        val isPlaying = currentPlayingUrl == sound.url

                        SoundItemView(
                            title = sound.name,
                            isPlaying = isPlaying,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            onIconClick = {
                                if (isPlaying) {
                                    audioUtils.stopCurrentSound()
                                    currentPlayingUrl = null
                                } else {
                                    audioUtils.stopCurrentSound()
                                    audioUtils.playStream(sound.url, sound.type, null)
                                    currentPlayingUrl = sound.url
                                }
                            }
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
