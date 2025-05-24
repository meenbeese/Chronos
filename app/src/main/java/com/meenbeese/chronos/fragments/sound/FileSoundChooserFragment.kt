package com.meenbeese.chronos.fragments.sound

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.SoundsAdapter
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.fragments.BasePagerFragment
import com.meenbeese.chronos.fragments.FileChooserFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "user_preferences")

class FileSoundChooserFragment : BaseSoundChooserFragment() {
    private lateinit var sounds: MutableList<SoundData>

    private val PREF_FILES_KEY = stringSetPreferencesKey("previousFiles")

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sound_chooser_file, container, false)

        view.findViewById<View>(R.id.addAudioFile).setOnClickListener { launchFileChooser() }
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)

        GlobalScope.launch(Dispatchers.Main) {
            val previousFiles = loadPreviousFiles()

            sounds = ArrayList()
            for (string in previousFiles) {
                val parts = string.split(SEPARATOR.toRegex()).toTypedArray()
                sounds.add(SoundData(parts[1], SoundData.TYPE_RINGTONE, parts[2]))
            }

            recycler.layoutManager = LinearLayoutManager(context)

            val adapter = SoundsAdapter(chronos!!, sounds)
            adapter.setListener(this@FileSoundChooserFragment)
            recycler.adapter = adapter
        }

        return view
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
            sounds.remove(it)
            sounds.add(0, it)

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
            } catch (e: NumberFormatException) {
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
