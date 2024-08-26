package com.meenbeese.chronos.data.preference

import androidx.fragment.app.FragmentActivity

import com.meenbeese.chronos.fragments.FileChooserFragment
import com.meenbeese.chronos.data.PreferenceData


/**
 * A preference item that allows the user to select
 * an image from a file (the resulting preference
 * contains a valid image path / URI).
 */
class ImageFilePreferenceData(
    private val preference: PreferenceData,
    name: Int
) : CustomPreferenceData(name) {
    override fun getValueName(holder: ViewHolder): String = ""

    override fun onClick(holder: ViewHolder) {
        val fragment = FileChooserFragment.newInstance(preference, "image/*")
        fragment.setCallback { _, uri ->
            preference.setValue(holder.context, uri)
        }
        val activity = holder.context as FragmentActivity
        activity.supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }
}
