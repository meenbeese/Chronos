package com.meenbeese.chronos.nav.destinations

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment

import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.ui.screens.AboutScreen
import com.meenbeese.chronos.utils.safeStartActivity

import java.util.Calendar

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                AboutScreen(
                    onOpenUrl = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        safeStartActivity(requireContext(), intent)
                    },
                    onSendEmail = { email ->
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:$email".toUri()
                            putExtra(Intent.EXTRA_SUBJECT, "Feedback for Chronos")
                        }
                        safeStartActivity(requireContext(), intent)
                    },
                    version = "Version ${BuildConfig.VERSION_NAME}",
                    year = Calendar.getInstance().get(Calendar.YEAR)
                )
            }
        }
    }
}