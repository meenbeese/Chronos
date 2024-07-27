package com.meenbeese.chronos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.core.content.ContextCompat

import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.R

import java.util.Calendar


class AboutFragment : BaseFragment() {

    private var appIcon: ImageView? = null
    private var appName: TextView? = null
    private var appDescription: TextView? = null
    private var versionInfo: TextView? = null
    private var engageTitle: TextView? = null
    private var forkGitHub: TextView? = null
    private var visitWebsite: TextView? = null
    private var sendEmail: TextView? = null
    private var copyrightInfo: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        appIcon = view.findViewById(R.id.app_icon)
        appIcon?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_launcher))

        appName = view.findViewById(R.id.app_name)
        appName?.text = getString(R.string.app_name)

        appDescription = view.findViewById(R.id.app_description)
        appDescription?.text = DESCRIPTION

        versionInfo = view.findViewById(R.id.version_info)
        versionInfo?.text = VERSION

        engageTitle = view.findViewById(R.id.engage_title)
        engageTitle?.text = getString(R.string.engage_title)

        forkGitHub = view.findViewById(R.id.fork_github)
        forkGitHub?.text = getString(R.string.fork_github)

        visitWebsite = view.findViewById(R.id.visit_website)
        visitWebsite?.text = getString(R.string.visit_website)

        sendEmail = view.findViewById(R.id.send_email)
        sendEmail?.text = getString(R.string.send_email)

        copyrightInfo = view.findViewById(R.id.copyright_info)
        copyrightInfo?.text = getString(R.string.copyright_info, YEAR)

        return view
    }

    companion object {
        const val DESCRIPTION =
            "Simple, yet customizable alarm clock app focused on simplicity, usability and design.\n\n" +
                    "• Custom backgrounds & ringtones\n" +
                    "• No unnecessary permissions\n" +
                    "• Dark, Light, AMOLED themes\n" +
                    "• Granular controls everywhere\n" +
                    "• Unique, minimal, efficient design\n" +
                    "• Portrait and landscape orientation\n" +
                    "• Countless default ringtones\n\n\n\n" +
                    "Made with ❤ in Canada."
        const val EMAIL = "meenbeese@tutanota.com"
        const val VERSION = "Version ${BuildConfig.VERSION_NAME}"
        const val WEBSITE = "https://meenbeese.is-a.dev"
        val YEAR = Calendar.getInstance().get(Calendar.YEAR)
    }
}
