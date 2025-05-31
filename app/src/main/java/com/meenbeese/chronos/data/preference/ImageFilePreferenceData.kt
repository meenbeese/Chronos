package com.meenbeese.chronos.data.preference

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope

import com.meenbeese.chronos.fragments.FileChooserFragment
import com.meenbeese.chronos.data.PreferenceData

import kotlinx.coroutines.launch

/**
 * A preference item that allows the user to select
 * an image from a file (the resulting preference
 * contains a valid image path / URI).
 */
class ImageFilePreferenceData(
    private val preference: PreferenceData,
    name: Int,
    @StringRes private val description: Int
) : CustomPreferenceData(name) {
    override fun getValueName(holder: ViewHolder): String = ""

    override fun bindViewHolder(holder: ViewHolder) {
        super.bindViewHolder(holder)
        holder.binding.description.setText(description)
    }

    override fun onClick(holder: ViewHolder) {
        val fragment = FileChooserFragment.newInstance(preference, "image/*")
        fragment.setCallback { _, uri ->
            val activity = holder.context as FragmentActivity
            activity.lifecycleScope.launch {
                preference.setValue(holder.context, uri)
            }
        }
        val activity = holder.context as FragmentActivity
        activity.supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }
}
