package com.meenbeese.chronos.nav.destinations

import android.content.Context
import android.content.Intent

import androidx.compose.runtime.Composable
import androidx.core.net.toUri

import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.ui.screens.AboutScreen
import com.meenbeese.chronos.utils.safeStartActivity

import java.util.Calendar

@Composable
fun AboutDestination(context: Context) {
    AboutScreen(
        onOpenUrl = { url ->
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            safeStartActivity(context, intent)
        },
        onSendEmail = { email ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:$email".toUri()
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for Chronos")
            }
            safeStartActivity(context, intent)
        },
        version = "Version ${BuildConfig.VERSION_NAME}",
        year = Calendar.getInstance().get(Calendar.YEAR)
    )
}
