package me.jfenn.alarmio.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.afollestad.aesthetic.Aesthetic.Companion.get

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

import me.jfenn.alarmio.R
import me.jfenn.alarmio.adapters.PreferenceAdapter
import me.jfenn.alarmio.data.PreferenceData
import me.jfenn.alarmio.data.preference.AboutPreferenceData
import me.jfenn.alarmio.data.preference.AlertWindowPreferenceData
import me.jfenn.alarmio.data.preference.BatteryOptimizationPreferenceData
import me.jfenn.alarmio.data.preference.BooleanPreferenceData
import me.jfenn.alarmio.data.preference.ImageFilePreferenceData
import me.jfenn.alarmio.data.preference.RingtonePreferenceData
import me.jfenn.alarmio.data.preference.ThemePreferenceData
import me.jfenn.alarmio.data.preference.TimePreferenceData
import me.jfenn.alarmio.data.preference.TimeZonesPreferenceData
import me.jfenn.alarmio.interfaces.ContextFragmentInstantiator


class SettingsFragment : BasePagerFragment(), Consumer<Any?> {
    private var recyclerView: RecyclerView? = null
    private var preferenceAdapter: PreferenceAdapter? = null
    private var colorPrimarySubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_recycler, container, false)
        recyclerView = v.findViewById(R.id.recycler)
        recyclerView?.layoutManager = GridLayoutManager(context, 1)
        recyclerView?.addItemDecoration(
            DividerItemDecoration(
                recyclerView?.context,
                DividerItemDecoration.VERTICAL
            )
        )
        val list = ArrayList(
            listOf(
                ThemePreferenceData(),
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
        )
        list.add(0, BatteryOptimizationPreferenceData())
        list.add(0, AlertWindowPreferenceData())
        list.add(AboutPreferenceData())
        preferenceAdapter = PreferenceAdapter(list)
        recyclerView?.adapter = preferenceAdapter
        colorPrimarySubscription = get()
            .colorPrimary()
            .subscribe(this)
        textColorPrimarySubscription = get()
            .textColorPrimary()
            .subscribe(this)
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        colorPrimarySubscription?.dispose()
        textColorPrimarySubscription?.dispose()
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
        recyclerView?.post { preferenceAdapter?.notifyDataSetChanged() }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun accept(o: Any?) {
        recyclerView?.post { preferenceAdapter?.notifyDataSetChanged() }
    }

    class Instantiator(context: Context?) : ContextFragmentInstantiator(context!!) {
        override fun getTitle(context: Context?, position: Int): String {
            return context!!.getString(R.string.title_settings)
        }

        override fun newInstance(position: Int): BasePagerFragment {
            return SettingsFragment()
        }
    }
}
