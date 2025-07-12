package com.meenbeese.chronos.ext

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast

import androidx.core.content.ContextCompat
import androidx.core.net.toUri

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceEntry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun openOverlaySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:${context.packageName}".toUri()
    )
    context.startActivity(intent)
}

fun handleBatteryOptimizationClick(context: Context) {
    val permission = Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS

    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = "package:${context.packageName}".toUri()
        }
        checkIntentAndStart(context, intent)
    } else {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        val started = checkIntentAndStart(context, intent)
        if (started) {
            Toast.makeText(
                context,
                R.string.msg_battery_optimizations_switch_enable,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

fun checkIntentAndStart(context: Context, intent: Intent): Boolean {
    val activity = intent.resolveActivity(context.packageManager)
    return if (activity != null) {
        context.startActivity(intent)
        true
    } else {
        false
    }
}

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

fun <T> PreferenceEntry<T>.getFlow(context: Context): Flow<T> {
    return context.dataStore.data.map { it[key] ?: defaultValue }
}
