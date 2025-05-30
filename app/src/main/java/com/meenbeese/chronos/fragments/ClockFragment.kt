package com.meenbeese.chronos.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.content.ContextCompat

import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.databinding.FragmentClockBinding
import com.meenbeese.chronos.interfaces.ContextFragmentInstantiator
import com.meenbeese.chronos.utils.ImageUtils.isBitmapDark
import com.meenbeese.chronos.utils.ImageUtils.toBitmap

import java.util.TimeZone

class ClockFragment : BasePagerFragment() {
    private var _binding: FragmentClockBinding? = null
    private val binding get() = _binding!!

    private var timezone: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClockBinding.inflate(inflater, container, false)

        if (arguments != null && requireArguments().containsKey(EXTRA_TIME_ZONE)) {
            timezone = arguments?.getString(EXTRA_TIME_ZONE)
            timezone?.let {
                binding.timeView.setTimezone(it)
                if (it != TimeZone.getDefault().id) {
                    binding.timezone.text = String.format(
                        "%s\n%s",
                        it.replace("_".toRegex(), " "),
                        TimeZone.getTimeZone(it).displayName
                    )
                }
            }
        }

        val textColor = getContrastingTextColorFromBg()
        binding.timezone.setTextColor(textColor)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getContrastingTextColorFromBg(): Int {
        val backgroundName = PreferenceData.BACKGROUND_IMAGE.getValue<String>(requireContext())
        val resName = backgroundName.substringAfter("/")
        val resId = resources.getIdentifier(resName, "drawable", requireContext().packageName)

        val drawable: Drawable? = ContextCompat.getDrawable(requireContext(), resId)
        val bitmap = drawable?.toBitmap()

        bitmap?.let {
            val isDark = isBitmapDark(it)
            return if (isDark) Color.LTGRAY else Color.DKGRAY
        }

        // Fallback color if bitmap couldn't be generated
        return Color.DKGRAY
    }

    override fun getTitle(context: Context?): String? {
        return timezone
    }

    class Instantiator(context: Context?, private val timezone: String?) :
        ContextFragmentInstantiator(
            context!!
        ) {
        override fun getTitle(context: Context?, position: Int): String? {
            return timezone
        }

        override fun newInstance(position: Int): BasePagerFragment {
            val args = Bundle()
            args.putString(EXTRA_TIME_ZONE, timezone)
            val fragment = ClockFragment()
            fragment.arguments = args
            return fragment
        }
    }

    companion object {
        const val EXTRA_TIME_ZONE = "com.meenbeese.chronos.fragments.ClockFragment.EXTRA_TIME_ZONE"
    }
}
