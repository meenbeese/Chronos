package com.meenbeese.chronos.data.preference

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

import androidx.appcompat.app.AppCompatDelegate

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * Allow the user to choose the theme of the
 * application.
 */
class ThemePreferenceData(
    context: Context?
) : BasePreferenceData<ThemePreferenceData.ViewHolder>() {
    var chronos: Chronos = context?.applicationContext as Chronos

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): BasePreferenceData.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_preference_theme, parent, false))
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        val adapter = ArrayAdapter.createFromResource(
            holder.itemView.context,
            R.array.array_themes,
            com.google.android.material.R.layout.support_simple_spinner_dropdown_item
        )

        holder.themeAutoCompleteTextView.setAdapter(adapter)

        val theme: Int = holder.chronos?.activityTheme ?: Chronos.THEME_AUTO
        if (theme != -1) {
            holder.themeAutoCompleteTextView.setText(adapter.getItem(theme), false)
        }

        holder.themeAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            if (holder.themeAutoCompleteTextView.text.toString() == adapter.getItem(position)) {
                GlobalScope.launch {
                    PreferenceData.THEME.setValue(holder.itemView.context, position)
                }
                applyTheme(position)
            }
        }
    }

    private fun applyTheme(theme: Int) {
        when (theme) {
            Chronos.THEME_AUTO -> {
                if (chronos.isNight) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    chronos.setTheme(R.style.AppTheme_Night)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    chronos.setTheme(R.style.AppTheme)
                }
            }
            Chronos.THEME_DAY -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                chronos.setTheme(R.style.AppTheme)
            }
            Chronos.THEME_NIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                chronos.setTheme(R.style.AppTheme_Night)
            }
            Chronos.THEME_AMOLED -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                chronos.setTheme(R.style.AppTheme_Amoled)
            }
        }
        chronos.recreate()
    }

    /**
     * Holds child views of the current item.
     */
    class ViewHolder(v: View) : BasePreferenceData.ViewHolder(v) {
        val themeAutoCompleteTextView: AutoCompleteTextView = v.findViewById(R.id.themeSpinner)
    }
}
