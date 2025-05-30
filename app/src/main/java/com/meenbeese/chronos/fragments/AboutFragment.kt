package com.meenbeese.chronos.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.core.content.ContextCompat
import androidx.core.net.toUri

import coil3.load
import coil3.request.transformations
import coil3.transform.CircleCropTransformation

import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.R
import com.meenbeese.chronos.databinding.FragmentAboutBinding

import java.util.Calendar

class AboutFragment : BaseFragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)

        binding.appIcon.load(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_launcher_round)) {
            transformations(CircleCropTransformation())
        }

        binding.appName.text = getString(R.string.app_name)
        binding.appDescription.text = getString(R.string.app_description)
        binding.featuresList.text = getString(R.string.feature_list)
        binding.madeWithLove.text = getString(R.string.made_with_love)
        binding.versionInfo.text = VERSION
        binding.engageTitle.text = getString(R.string.engage_title)

        binding.forkGithub.text = getString(R.string.fork_github)
        binding.forkGithub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, GITHUB.toUri())
            startActivity(intent)
        }

        binding.visitWebsite.text = getString(R.string.visit_website)
        binding.visitWebsite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, WEBSITE.toUri())
            startActivity(intent)
        }

        binding.sendEmail.text = getString(R.string.send_email)
        binding.sendEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:$EMAIL".toUri()
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for Chronos")
            }
            startActivity(intent)
        }

        val copyrightText = getString(R.string.copyright_info, YEAR)
        binding.copyrightInfo.text = copyrightText
        binding.copyrightInfo.setOnClickListener {
            Toast.makeText(requireContext(), copyrightText, Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val EMAIL = "kuzeybilgin@proton.me"
        const val GITHUB = "https://github.com/meenbeese/Chronos"
        const val VERSION = "Version ${BuildConfig.VERSION_NAME}"
        const val WEBSITE = "https://kuzey.is-a.dev"
        val YEAR = Calendar.getInstance().get(Calendar.YEAR)
    }
}
