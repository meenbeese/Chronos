package com.meenbeese.chronos.data.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.net.toUri

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meenbeese.chronos.R
import com.meenbeese.chronos.databinding.ItemPreferenceBooleanBinding

/**
 * A preference item allowing the user to grant the
 * overlay permissions / alert window to ignore background
 * restrictions when starting an activity.
 *
 * @see https://developer.android.com/guide/components/activities/background-starts
 */
class AlertWindowPreferenceData
    : BasePreferenceData<AlertWindowPreferenceData.ViewHolder>() {

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): BasePreferenceData.ViewHolder {
        val binding = ItemPreferenceBooleanBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        holder.binding.title.setText(R.string.info_background_permissions_title)
        holder.binding.description.visibility = View.GONE
        holder.binding.toggle.setOnCheckedChangeListener(null)
        holder.binding.toggle.isClickable = false
        holder.binding.toggle.isChecked = Settings.canDrawOverlays(holder.binding.root.context)
        holder.binding.root.setOnClickListener {
            if (!Settings.canDrawOverlays(holder.binding.root.context)) {
                showAlert(holder)
            } else {
                showActivity(holder.binding.root.context)
            }
        }
    }

    private fun showAlert(holder: ViewHolder) {
        MaterialAlertDialogBuilder(holder.binding.root.context, if(holder.chronos!!.isDarkTheme()) com.google.android.material.R.style.Theme_MaterialComponents_Dialog_Alert else com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog_Alert)
            .setTitle(holder.context.getString(R.string.info_background_permissions_title))
            .setMessage(holder.context.getString(R.string.info_background_permissions_body))
            .setPositiveButton(holder.binding.root.context.getString(android.R.string.ok)){_, _ ->  showActivity(holder.context)}
            .setNegativeButton(holder.binding.root.context.getString(android.R.string.cancel), null)
            .show()
    }

    private fun showActivity(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.applicationContext.packageName}".toUri())
        )
    }

    /**
     * Holds child views of the current item.
     */
    inner class ViewHolder(val binding: ItemPreferenceBooleanBinding) : BasePreferenceData.ViewHolder(binding.root)
}
