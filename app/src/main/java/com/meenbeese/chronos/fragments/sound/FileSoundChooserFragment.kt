package com.meenbeese.chronos.fragments.sound

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.SoundsAdapter
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.fragments.BasePagerFragment
import com.meenbeese.chronos.fragments.FileChooserFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener


class FileSoundChooserFragment : BaseSoundChooserFragment() {
    private lateinit var prefs: SharedPreferences
    private lateinit var sounds: MutableList<SoundData>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sound_chooser_file, container, false)
        prefs = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }!!
        view.findViewById<View>(R.id.addAudioFile).setOnClickListener { launchFileChooser() }
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        val previousFiles = ArrayList(prefs.getStringSet(PREF_FILES, HashSet())!!)
        previousFiles.sortWith { o1, o2 ->
            try {
                Integer.parseInt(o1.split(SEPARATOR.toRegex())[0]) - Integer.parseInt(o2.split(SEPARATOR.toRegex())[0])
            } catch (e: NumberFormatException) {
                0
            }
        }

        sounds = ArrayList()
        for (string in previousFiles) {
            val parts = string.split(SEPARATOR.toRegex()).toTypedArray()
            sounds.add(SoundData(parts[1], SoundData.TYPE_RINGTONE, parts[2]))
        }

        recycler.layoutManager = LinearLayoutManager(context)

        val adapter = SoundsAdapter(chronos!!, sounds)
        adapter.setListener(this)
        recycler.adapter = adapter

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

    override fun onSoundChosen(sound: SoundData?) {
        super.onSoundChosen(sound)
        sound?.let {
            sounds.remove(it)
            sounds.add(0, it)
            val files: MutableSet<String> = HashSet()
            for (i in sounds.indices) {
                files.add(i.toString() + SEPARATOR + sounds[i].name + SEPARATOR + sounds[i].url)
            }
            prefs.edit()?.putStringSet(PREF_FILES, files)?.apply()
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
        private const val PREF_FILES = "previousFiles"
    }
}
