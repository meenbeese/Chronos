package com.meenbeese.chronos.data.preference

import android.content.Intent

import com.meenbeese.chronos.activities.FileChooserActivity
import com.meenbeese.chronos.data.PreferenceData


/**
 * A preference item that allows the user to select
 * an image from a file (the resulting preference
 * contains a valid image path / URI).
 */
class ImageFilePreferenceData(private val preference: PreferenceData, name: Int) : CustomPreferenceData(name) {

    override fun getValueName(holder: ViewHolder): String = ""

    override fun onClick(holder: ViewHolder) {
        holder.context.startActivity(Intent(holder.context, FileChooserActivity::class.java).apply {
            putExtra(FileChooserActivity.EXTRA_PREF, preference)
            putExtra(FileChooserActivity.EXTRA_TYPE, "image/*")
        })
    }
}
