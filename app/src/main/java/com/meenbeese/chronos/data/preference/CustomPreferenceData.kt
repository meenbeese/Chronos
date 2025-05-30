package com.meenbeese.chronos.data.preference

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.meenbeese.chronos.databinding.ItemPreferenceCustomBinding

/**
 * A simple preference item to bind a title
 * and text value of a preference to a basic
 * item view.
 */
abstract class CustomPreferenceData(
    private val name: Int
) : BasePreferenceData<CustomPreferenceData.ViewHolder>() {
    /**
     * Get the name of the current value of the preference.
     */
    abstract fun getValueName(holder: ViewHolder): String?

    /**
     * Called when the preference is clicked.
     */
    abstract fun onClick(holder: ViewHolder)

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val binding = ItemPreferenceCustomBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun bindViewHolder(holder: ViewHolder) {
        holder.binding.name.setText(name)
        holder.binding.value.text = getValueName(holder) ?: run {
            holder.binding.value.visibility = View.GONE; null
        }

        holder.binding.root.setOnClickListener { onClick(holder) }
    }

    /**
     * Holds child views of the current item.
     */
    class ViewHolder(val binding: ItemPreferenceCustomBinding) : BasePreferenceData.ViewHolder(binding.root)
}
