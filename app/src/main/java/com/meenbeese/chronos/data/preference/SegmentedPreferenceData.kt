package com.meenbeese.chronos.data.preference

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.databinding.ItemPreferenceSegmentedBinding

import kotlinx.coroutines.runBlocking

class SegmentedPreferenceData(
    private val context: Context,
    private val nameRes: Int,
    private val onSelectionChanged: () -> Unit
) : BasePreferenceData<SegmentedPreferenceData.ViewHolder>() {

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val binding = ItemPreferenceSegmentedBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun bindViewHolder(holder: ViewHolder) {
        holder.binding.name.setText(nameRes)

        val isColorSelected = Preferences.COLORFUL_BACKGROUND.get(context)
        val group = holder.binding.segmentedGroup

        group.check(if (isColorSelected) R.id.button_color else R.id.button_image)

        group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            runBlocking {
                Preferences.COLORFUL_BACKGROUND.set(context, checkedId == R.id.button_color)
                onSelectionChanged()
            }
        }
    }

    /**
     * Holds child views of the current item.
     */
    inner class ViewHolder(val binding: ItemPreferenceSegmentedBinding) : BasePreferenceData.ViewHolder(binding.root)
}
