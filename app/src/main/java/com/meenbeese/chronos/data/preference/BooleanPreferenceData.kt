package com.meenbeese.chronos.data.preference

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.annotation.StringRes

import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.databinding.ItemPreferenceBooleanBinding

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Allow the user to choose from a simple boolean
 * using a switch item view.
 */
class BooleanPreferenceData(
    private val preference: PreferenceData,
    @StringRes private val title: Int,
    @StringRes private val description: Int
) : BasePreferenceData<BooleanPreferenceData.ViewHolder>() {

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): BasePreferenceData.ViewHolder {
        val binding = ItemPreferenceBooleanBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        holder.binding.title.setText(title)
        holder.binding.description.setText(description)

        val currentValue = preference.getValue<Boolean>(holder.binding.root.context)

        holder.binding.toggle.setOnCheckedChangeListener(null)
        holder.binding.toggle.isChecked = currentValue
        holder.binding.toggle.setOnCheckedChangeListener { compoundButton, b ->
            GlobalScope.launch {
                preference.setValue(compoundButton.context, b)
            }
        }
    }

    /**
     * Holds child views of the current item.
     */
    inner class ViewHolder(val binding: ItemPreferenceBooleanBinding) : BasePreferenceData.ViewHolder(binding.root)
}
