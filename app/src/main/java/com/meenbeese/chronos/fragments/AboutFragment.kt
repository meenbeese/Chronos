package com.meenbeese.chronos.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.core.content.ContextCompat

import coil3.load
import coil3.request.transformations
import coil3.transform.CircleCropTransformation

import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.R

import java.util.Calendar


class AboutFragment : BaseFragment() {
    private var appIcon: ImageView? = null
    private var appName: TextView? = null
    private var appDescription: TextView? = null
    private var featuresList: TextView? = null
    private var madeWithLove: TextView? = null
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
        appIcon?.load(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_launcher_round)) {
            transformations(CircleCropTransformation())
        }

        appName = view.findViewById(R.id.app_name)
        appName?.text = getString(R.string.app_name)

        appDescription = view.findViewById(R.id.app_description)
        appDescription?.text = getString(R.string.app_description)

        featuresList = view.findViewById(R.id.features_list)
        featuresList?.text = getString(R.string.feature_list)

        madeWithLove = view.findViewById(R.id.made_with_love)
        madeWithLove?.text = getString(R.string.made_with_love)

        versionInfo = view.findViewById(R.id.version_info)
        versionInfo?.text = VERSION

        engageTitle = view.findViewById(R.id.engage_title)
        engageTitle?.text = getString(R.string.engage_title)

        forkGitHub = view.findViewById(R.id.fork_github)
        forkGitHub?.text = getString(R.string.fork_github)
        forkGitHub?.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB))
            startActivity(intent)
        }

        visitWebsite = view.findViewById(R.id.visit_website)
        visitWebsite?.text = getString(R.string.visit_website)
        visitWebsite?.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE))
            startActivity(intent)
        }

        sendEmail = view.findViewById(R.id.send_email)
        sendEmail?.text = getString(R.string.send_email)
        sendEmail?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$EMAIL")
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for Chronos")
            }
            startActivity(intent)
        }

        copyrightInfo = view.findViewById(R.id.copyright_info)
        copyrightInfo?.text = getString(R.string.copyright_info, YEAR)
        copyrightInfo?.setOnClickListener {
            val copyrightText = getString(R.string.copyright_info, YEAR)
            Toast.makeText(requireContext(), copyrightText, Toast.LENGTH_SHORT).show()
        }

        return view
    }

    companion object {
        const val EMAIL = "kuzeybilgin@proton.me"
        const val GITHUB = "https://github.com/meenbeese/Chronos"
        const val VERSION = "Version ${BuildConfig.VERSION_NAME}"
        const val WEBSITE = "https://kuzey.is-a.dev"
        val YEAR = Calendar.getInstance().get(Calendar.YEAR)
    }
}
