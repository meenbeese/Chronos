package com.meenbeese.chronos.data.preference

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.databinding.ItemPreferenceThemeBinding
import com.meenbeese.chronos.utils.Theme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

/**
 * Allow the user to choose the theme of the
 * application.
 */
class ThemePreferenceData(
    context: Context,
    private val scope: CoroutineScope
) : BasePreferenceData<ThemePreferenceData.ViewHolder>() {
    var chronos: Chronos = context.applicationContext as Chronos

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): BasePreferenceData.ViewHolder {
        val binding = ItemPreferenceThemeBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        val context = holder.binding.root.context
        val adapter = ArrayAdapter.createFromResource(
            context,
            R.array.array_themes,
            com.google.android.material.R.layout.support_simple_spinner_dropdown_item
        )

        holder.binding.themeSpinner.setAdapter(adapter)

        val theme: Theme = holder.chronos?.activityTheme ?: Theme.AUTO
        holder.binding.themeSpinner.setText(adapter.getItem(theme.value), false)
        holder.binding.themeSpinner.setDropDownBackgroundResource(R.color.colorForeground)
        holder.binding.themeDropdown.post {
            holder.binding.themeDropdown.requestLayout()
        }
        holder.binding.themeSpinner.setOnItemClickListener { _, _, position, _ ->
            if (holder.binding.themeSpinner.text.toString() == adapter.getItem(position)) {
                val selectedTheme = Theme.fromInt(position)
                val activity = holder.binding.root.context as? Activity
                if (activity != null) {
                    scope.launch {
                        chronos.applyAndSaveTheme(context, selectedTheme)
                        activity.recreate()
                    }
                }
            }
        }
    }

    /**
     * Holds child views of the current item.
     */
    class ViewHolder(val binding: ItemPreferenceThemeBinding) : BasePreferenceData.ViewHolder(binding.root)
}
