package com.meenbeese.chronos.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.util.Consumer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.PreferenceAdapter
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.data.preference.AboutPreferenceData
import com.meenbeese.chronos.data.preference.AlertWindowPreferenceData
import com.meenbeese.chronos.data.preference.BasePreferenceData
import com.meenbeese.chronos.data.preference.BatteryOptimizationPreferenceData
import com.meenbeese.chronos.data.preference.BooleanPreferenceData
import com.meenbeese.chronos.data.preference.ImageFilePreferenceData
import com.meenbeese.chronos.data.preference.RingtonePreferenceData
import com.meenbeese.chronos.data.preference.ThemePreferenceData
import com.meenbeese.chronos.data.preference.TimePreferenceData
import com.meenbeese.chronos.data.preference.TimeZonesPreferenceData
import com.meenbeese.chronos.interfaces.ContextFragmentInstantiator


class SettingsFragment : BasePagerFragment(), Consumer<Any?> {
    private lateinit var recyclerView: RecyclerView
    private var preferenceAdapter: PreferenceAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_recycler, container, false)
        recyclerView = v.findViewById(R.id.recycler)
        recyclerView.layoutManager = GridLayoutManager(context, 1)

        val dataList = mutableListOf(
            ThemePreferenceData(context),
            ImageFilePreferenceData(
                PreferenceData.BACKGROUND_IMAGE,
                R.string.title_background_image
            ),
            BooleanPreferenceData(
                PreferenceData.RINGING_BACKGROUND_IMAGE,
                R.string.title_ringing_background_image,
                R.string.desc_ringing_background_image
            ),
            TimeZonesPreferenceData(
                PreferenceData.TIME_ZONE_ENABLED,
                R.string.title_time_zones
            ),
            RingtonePreferenceData(
                PreferenceData.DEFAULT_ALARM_RINGTONE,
                R.string.title_default_alarm_ringtone
            ),
            RingtonePreferenceData(
                PreferenceData.DEFAULT_TIMER_RINGTONE,
                R.string.title_default_timer_ringtone
            ),
            BooleanPreferenceData(
                PreferenceData.SLEEP_REMINDER,
                R.string.title_sleep_reminder,
                R.string.desc_sleep_reminder
            ),
            TimePreferenceData(
                PreferenceData.SLEEP_REMINDER_TIME,
                R.string.title_sleep_reminder_time
            ),
            BooleanPreferenceData(
                PreferenceData.SLOW_WAKE_UP,
                R.string.title_slow_wake_up,
                R.string.desc_slow_wake_up
            ),
            TimePreferenceData(
                PreferenceData.SLOW_WAKE_UP_TIME,
                R.string.title_slow_wake_up_time
            )
        )
        dataList.add(0, BatteryOptimizationPreferenceData())
        dataList.add(0, AlertWindowPreferenceData())
        dataList.add(AboutPreferenceData(requireContext()))
        preferenceAdapter = PreferenceAdapter(dataList as MutableList<BasePreferenceData<BasePreferenceData.ViewHolder>>)
        recyclerView.adapter = preferenceAdapter

        return v
    }

    override fun getTitle(context: Context?): String? {
        return context?.getString(R.string.title_settings)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        recyclerView.post { preferenceAdapter?.notifyDataSetChanged() }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun accept(o: Any?) {
        recyclerView.post { preferenceAdapter?.notifyDataSetChanged() }
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
