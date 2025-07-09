package com.meenbeese.chronos.fragments.sound

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.ext.dataStore
import com.meenbeese.chronos.fragments.BasePagerFragment
import com.meenbeese.chronos.fragments.FileChooserFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.meenbeese.chronos.utils.AudioUtils
import com.meenbeese.chronos.views.SoundItemView

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@UnstableApi
class FileSoundChooserFragment : BaseSoundChooserFragment() {
    private var sounds by mutableStateOf<List<SoundData>>(emptyList())
    private val PREF_FILES_KEY = stringSetPreferencesKey("previousFiles")
    private lateinit var audioUtils: AudioUtils

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        audioUtils = AudioUtils(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LaunchedEffect(Unit) {
                        val previousFiles = loadPreviousFiles()
                        val items = previousFiles.mapNotNull { str ->
                            val parts = str.split(SEPARATOR)
                            if (parts.size == 3) SoundData(parts[1], SoundData.TYPE_RINGTONE, parts[2]) else null
                        }
                        sounds = items
                    }

                    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { launchFileChooser() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Audio File")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(sounds) { sound ->
                                val isPlaying = currentlyPlayingUrl == sound.url

                                SoundItemView(
                                    title = sound.name,
                                    isPlaying = isPlaying,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    onIconClick = {
                                        if (isPlaying) {
                                            audioUtils.stopCurrentSound()
                                            currentlyPlayingUrl = null
                                        } else {
                                            audioUtils.stopCurrentSound()
                                            audioUtils.playStream(sound.url, sound.type, null)
                                            currentlyPlayingUrl = sound.url
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchFileChooser() {
        val fragment = FileChooserFragment.newInstance(null, TYPE_AUDIO)
        fragment.setCallback { name, uri ->
            onSoundChosen(SoundData(name, SoundData.TYPE_RINGTONE, uri))
        }

        val activity = context as FragmentActivity
        activity.supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onSoundChosen(sound: SoundData?) {
        super.onSoundChosen(sound)
        sound?.let {
            sounds = (sounds - it).toMutableList().apply { add(0, it) }
            GlobalScope.launch {
                savePreviousFiles()
            }
        }
    }

    private suspend fun loadPreviousFiles(): List<String> {
        val preferences = context?.dataStore?.data?.first()
        val previousFiles = preferences?.get(PREF_FILES_KEY) ?: emptySet()
        return previousFiles.toList().sortedWith { o1, o2 ->
            try {
                Integer.parseInt(o1.split(SEPARATOR.toRegex())[0]) - Integer.parseInt(o2.split(SEPARATOR.toRegex())[0])
            } catch (_: NumberFormatException) {
                0
            }
        }
    }

    private suspend fun savePreviousFiles() {
        val files: MutableSet<String> = HashSet()
        for (i in sounds.indices) {
            files.add(i.toString() + SEPARATOR + sounds[i].name + SEPARATOR + sounds[i].url)
        }

        context?.dataStore?.edit { preferences ->
            preferences[PREF_FILES_KEY] = files
        }
    }

    override fun getTitle(context: Context?): String? {
        return context?.getString(R.string.title_files)
    }

    internal class Instantiator(context: Context?, listener: SoundChooserListener?) :
        BaseSoundChooserFragment.Instantiator(context, listener) {
        override fun newInstance(
            position: Int,
            listener: SoundChooserListener?
        ): BasePagerFragment {
            val fragment: BaseSoundChooserFragment = FileSoundChooserFragment()
            fragment.setListener(listener)
            return fragment
        }

        override fun getTitle(context: Context?, position: Int): String? {
            return context?.getString(R.string.title_files)
        }
    }

    companion object {
        private const val TYPE_AUDIO = "audio/*"
        private const val SEPARATOR = ":ChronosFileSound:"
    }
}
