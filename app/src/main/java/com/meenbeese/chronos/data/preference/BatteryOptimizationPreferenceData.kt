package com.meenbeese.chronos.data.preference

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast

import androidx.core.content.ContextCompat
import androidx.core.net.toUri

import com.meenbeese.chronos.R

/**
 * A preference item allowing the user to select to
 * ignore battery optimizations to improve stability.
 */
class BatteryOptimizationPreferenceData
    : CustomPreferenceData(R.string.title_ignore_battery_optimizations) {
    override fun getValueName(holder: ViewHolder): String? = null

    override fun onClick(holder: ViewHolder) {
        val context = holder.context

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = ("package:" + context.applicationContext.packageName).toUri()
            checkIntentAndStart(context, intent)
        } else {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            if (checkIntentAndStart(context, intent))
                Toast.makeText(context, R.string.msg_battery_optimizations_switch_enable, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkIntentAndStart(context: Context, intent: Intent): Boolean {
        intent.resolveActivity(context.packageManager)?.let {
            context.startActivity(intent)
            return true
        }

        return false
    }
}
