package com.meenbeese.chronos.fragments.sound

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.meenbeese.chronos.R
import com.meenbeese.chronos.activities.FileChooserActivity
import com.meenbeese.chronos.adapters.SoundsAdapter
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.fragments.BasePagerFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener


class FileSoundChooserFragment : BaseSoundChooserFragment() {
    private var prefs: SharedPreferences? = null
    private var sounds: MutableList<SoundData>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sound_chooser_file, container, false)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
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

        val sounds = ArrayList<SoundData>()
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
        val intent = Intent(context, FileChooserActivity::class.java)
        intent.putExtra(FileChooserActivity.EXTRA_TYPE, TYPE_AUDIO)
        startActivityForResult(intent, REQUEST_AUDIO)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_AUDIO && resultCode == Activity.RESULT_OK && data != null) {
            var name: String? = "Audio File"
            if (data.hasExtra("name")) name = data.getStringExtra("name")
            onSoundChosen(SoundData(name!!, SoundData.TYPE_RINGTONE, data.dataString!!))
        }
    }

    override fun onSoundChosen(sound: SoundData?) {
        super.onSoundChosen(sound)
        sound?.let {
            sounds?.remove(it)
            sounds?.add(0, it)
            val files: MutableSet<String> = HashSet()
            for (i in sounds!!.indices) {
                files.add(i.toString() + SEPARATOR + sounds!![i].name + SEPARATOR + sounds!![i].url)
            }
            prefs?.edit()?.putStringSet(PREF_FILES, files)?.apply()
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
        private const val REQUEST_AUDIO = 285
        private const val TYPE_AUDIO = "audio/*"
        private const val SEPARATOR = ":ChronosFileSound:"
        private const val PREF_FILES = "previousFiles"
    }
}
