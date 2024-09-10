package com.meenbeese.chronos.data.preference

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.annotation.StringRes

import com.google.android.material.switchmaterial.SwitchMaterial
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData


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
        return ViewHolder(inflater.inflate(R.layout.item_preference_boolean, parent, false))
    }

    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        holder.title.setText(title)
        holder.description.setText(description)
        holder.toggle.setOnCheckedChangeListener(null)
        holder.toggle.isChecked = preference.getValue(holder.itemView.context, false) ?: false
        holder.toggle.setOnCheckedChangeListener { compoundButton, b -> preference.setValue(compoundButton.context, b) }
    }

    /**
     * Holds child views of the current item.
     */
    inner class ViewHolder(v: View) : BasePreferenceData.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val description: TextView = v.findViewById(R.id.description)
        val toggle: SwitchMaterial = v.findViewById(R.id.toggle)
    }
}
