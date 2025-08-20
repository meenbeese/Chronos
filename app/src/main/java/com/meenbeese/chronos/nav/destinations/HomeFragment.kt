package com.meenbeese.chronos.nav.destinations

import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.toEntity
import com.meenbeese.chronos.db.AlarmRepository
import com.meenbeese.chronos.db.AlarmViewModel
import com.meenbeese.chronos.db.AlarmViewModelFactory
import com.meenbeese.chronos.interfaces.AlarmNavigator
import com.meenbeese.chronos.db.TimerAlarmRepository
import com.meenbeese.chronos.services.TimerService
import com.meenbeese.chronos.ui.screens.HomeScreen
import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.koin.android.ext.android.inject

import java.util.Calendar
import java.util.Date

@UnstableApi
class HomeFragment : BaseFragment() {
    private val isBottomSheetExpanded = mutableStateOf(false)
    private val repo: TimerAlarmRepository by inject()
    private val alarmRepo: AlarmRepository by inject()
    private val alarmViewModel: AlarmViewModel by lazy {
        ViewModelProvider(this, AlarmViewModelFactory(alarmRepo))[AlarmViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val alarms by alarmViewModel.alarms.observeAsState(emptyList())
                val intentAction = arguments?.getString(INTENT_ACTION)

                HomeScreen(
                    alarms = alarms,
                    isBottomSheetExpanded = isBottomSheetExpanded,
                    onAlarmUpdated = { alarmData ->
                        CoroutineScope(Dispatchers.IO).launch {
                            alarmViewModel.update(alarmData.toEntity())
                        }
                    },
                    onAlarmDeleted = { alarmData ->
                        CoroutineScope(Dispatchers.IO).launch {
                            alarmViewModel.delete(alarmData.toEntity())
                        }
                    },
                    onScheduleAlarm = { h, m -> scheduleAlarm(h, m) },
                    onScheduleWatch = { scheduleWatch() },
                    onScheduleTimer = { h, m, s, ring, vibrate -> scheduleTimer(h, m, s, ring, vibrate) },
                    navigateToNearestAlarm = { navigateToNearestAlarm() },
                    intentAction = intentAction
                )
            }
        }
    }

    /**
     * Open the alarm scheduler dialog to allow the user to create
     * a new alarm.
     */
    private fun scheduleAlarm(hour: Int, minute: Int) {
        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (BuildConfig.DEBUG) add(Calendar.MINUTE, 1)
        }.timeInMillis

        val alarm = AlarmData(
            id = 0,
            name = null,
            time = Calendar.getInstance().apply { timeInMillis = time },
            isEnabled = true,
            days = MutableList(7) { false },
            isVibrate = true,
            sound = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            val entity = alarm.toEntity()
            val id = alarmViewModel.insertAndReturnId(entity)
            alarm.id = id.toInt()
            alarm.set(requireContext())
        }

        val formatted = FormatUtils.formatShort(requireContext(), Date(time))
        Toast.makeText(requireContext(), "Alarm set for $formatted", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleWatch() {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_up_sheet,
                R.anim.slide_out_up_sheet,
                R.anim.slide_in_down_sheet,
                R.anim.slide_out_down_sheet
            )
            .replace(R.id.fragment, StopwatchFragment())
            .addToBackStack(null)
            .commit()
    }

    /**
     * Open the timer scheduler dialog to allow the user to start
     * a timer.
     */
    private fun scheduleTimer(
        hours: Int,
        minutes: Int,
        seconds: Int,
        ringtone: SoundData?,
        isVibrate: Boolean
    ) {
        val context = requireContext()
        val totalMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L

        if (totalMillis <= 0) {
            Toast.makeText(context, "Invalid timer duration", Toast.LENGTH_SHORT).show()
            return
        }

        val timer = repo.newTimer()
        timer.setDuration(totalMillis, context)
        timer.setVibrate(context, isVibrate)
        timer.setSound(context, ringtone)
        timer[context] = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        TimerService.Companion.startService(context)

        val args = Bundle().apply {
            putParcelable(TimerFragment.Companion.EXTRA_TIMER, timer)
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_up_sheet,
                R.anim.slide_out_up_sheet,
                R.anim.slide_in_down_sheet,
                R.anim.slide_out_down_sheet
            )
            .replace(R.id.fragment, TimerFragment().apply { arguments = args })
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToNearestAlarm() {
        val allAlarms = repo.alarms

        val alarmsWithNextTrigger = allAlarms
            .filter { it.isEnabled }
            .mapNotNull { alarm -> alarm.getNext()?.timeInMillis?.let { alarm to it } }

        val targetAlarm = alarmsWithNextTrigger
            .minByOrNull { it.second }?.first
            ?: allAlarms
                .mapNotNull { alarm -> alarm.getNext()?.timeInMillis?.let { alarm to it } }
                .minByOrNull { it.second }
                ?.first
            ?: return

        val fragment = parentFragmentManager.fragments
            .filterIsInstance<AlarmNavigator>()
            .firstOrNull()

        fragment?.jumpToAlarm(targetAlarm.id, openEditor = true)
    }

    companion object {
        const val INTENT_ACTION = "com.meenbeese.chronos.HomeFragment.INTENT_ACTION"
    }
}
