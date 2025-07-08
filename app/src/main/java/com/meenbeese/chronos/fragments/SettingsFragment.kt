package com.meenbeese.chronos.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ComposeView
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.interfaces.ContextFragmentInstantiator
import com.meenbeese.chronos.screens.SettingsScreen

class SettingsFragment : BasePagerFragment() {
    @ExperimentalMaterial3Api
    @UnstableApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SettingsScreen(
                    context = requireContext(),
                    chronos = chronos!!
                )
            }
        }
    }

    override fun getTitle(context: Context?): String? {
        return context?.getString(R.string.title_settings)
    }

    class Instantiator(context: Context?) : ContextFragmentInstantiator(context!!) {
        override fun getTitle(context: Context?, position: Int): String? {
            return context?.getString(R.string.title_settings)
        }

        override fun newInstance(position: Int): BasePagerFragment {
            return SettingsFragment()
        }
    }
}
