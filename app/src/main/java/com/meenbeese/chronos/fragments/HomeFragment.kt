package com.meenbeese.chronos.fragments

import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.ui.dialogs.TimerFactoryDialog
import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.toEntity
import com.meenbeese.chronos.db.AlarmViewModel
import com.meenbeese.chronos.db.AlarmViewModelFactory
import com.meenbeese.chronos.ui.dialogs.TimeChooserDialog
import com.meenbeese.chronos.ext.getFlow
import com.meenbeese.chronos.interfaces.AlarmNavigator
import com.meenbeese.chronos.ui.screens.AlarmsScreen
import com.meenbeese.chronos.ui.screens.ClockScreen
import com.meenbeese.chronos.ui.screens.SettingsScreen
import com.meenbeese.chronos.services.TimerService
import com.meenbeese.chronos.ui.views.AnimatedFabMenu
import com.meenbeese.chronos.ui.views.ClockPageView
import com.meenbeese.chronos.ui.views.FabItem
import com.meenbeese.chronos.ui.views.HomeBottomSheet
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.utils.ImageUtils.getContrastingTextColorFromBg
import com.meenbeese.chronos.utils.ImageUtils.rememberBackgroundPainterState

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@UnstableApi
class HomeFragment : BaseFragment() {
    private val isBottomSheetExpanded = mutableStateOf(false)
    private lateinit var alarmViewModel: AlarmViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val app = requireActivity().application as Chronos
        val factory = AlarmViewModelFactory(app.repository)

        alarmViewModel = ViewModelProvider(this, factory)[AlarmViewModel::class.java]
        alarmViewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            Log.d("HomeFragment", "Alarms updated, size: ${alarms.size}")
            if (alarms.isEmpty() && !isBottomSheetExpanded.value) {
                isBottomSheetExpanded.value = true
            }
        }

        val homeTabs = listOf(getString(R.string.title_alarms), getString(R.string.title_settings))
        val selectedTabIndex = mutableIntStateOf(0)

        val timerItem = FabItem(icon = R.drawable.ic_timer, text = R.string.title_set_timer)
        val watchItem = FabItem(icon = R.drawable.ic_stopwatch, text = R.string.title_set_stopwatch)
        val alarmItem = FabItem(icon = R.drawable.ic_alarm_add, text = R.string.title_set_alarm)

        return ComposeView(requireContext()).apply {
            setContent {
                var showAlarmDialog by remember { mutableStateOf(false) }
                var showTimerDialog by remember { mutableStateOf(false) }

                val clockBackground = rememberBackgroundPainterState(isAlarm = false)

                val timeZoneEnabled by Preferences.TIME_ZONE_ENABLED.getFlow(requireContext()).collectAsState(initial = false)
                val selectedZonesCsv by Preferences.TIME_ZONES.getFlow(requireContext()).collectAsState(initial = "")

                val alarms by alarmViewModel.alarms.observeAsState(emptyList())

                val selectedZones = buildList {
                    add(TimeZone.getDefault().id)
                    if (timeZoneEnabled) {
                        selectedZonesCsv
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() && TimeZone.getAvailableIDs().contains(it) }
                            .forEach { add(it) }
                    }
                }

                val clockScreens = selectedZones.map {
                    @Composable {
                        ClockScreen(
                            timezoneId = it,
                            onClockTap = {
                                if (Preferences.SCROLL_TO_NEXT.get(requireContext())) {
                                    navigateToNearestAlarm()
                                }
                            },
                            getTextColor = {
                                getContrastingTextColorFromBg(requireContext())
                            }
                        )
                    }
                }

                /*
                 * Check actions passed from MainActivity; open timer/alarm
                 * schedulers if necessary.
                 */
                LaunchedEffect(Unit) {
                    when (arguments?.getString(INTENT_ACTION)) {
                        AlarmClock.ACTION_SET_ALARM -> showAlarmDialog = true
                        AlarmClock.ACTION_SET_TIMER -> showTimerDialog = true
                    }
                }

                /*
                 * Observe alarms using LiveData and update the list and
                 * UI when an alarm is updated or deleted.
                 */
                LaunchedEffect(alarms) {
                    Log.d("HomeFragment", "Alarms updated, size: ${alarms.size}")
                    if (alarms.isEmpty() && !isBottomSheetExpanded.value) {
                        isBottomSheetExpanded.value = true
                    }
                }

                val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600

                if (isTablet) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        ClockPageView(
                            fragments = clockScreens,
                            backgroundPainter = clockBackground!!,
                            pageIndicatorVisible = clockScreens.size > 1,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(8.dp)
                        ) {
                            HomeBottomSheet(
                                tabs = homeTabs,
                                isTablet = true,
                                initialTabIndex = selectedTabIndex.intValue,
                                onTabChanged = { selectedTabIndex.intValue = it },
                                heightFraction = 0.5f + 0.035f // Cover rounded edges
                            ) { page ->
                                if (page == 0) {
                                    AlarmsScreen(
                                        alarms = alarms,
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
                                        onScrolledToEnd = { },
                                        isBottomSheetExpanded = isBottomSheetExpanded
                                    )
                                } else {
                                    SettingsScreen(
                                        context = requireContext(),
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ClockPageView(
                            fragments = clockScreens,
                            backgroundPainter = clockBackground!!,
                            pageIndicatorVisible = clockScreens.size > 1,
                            modifier = Modifier.fillMaxHeight(0.5f)
                        )

                        HomeBottomSheet(
                            tabs = homeTabs,
                            isTablet = false,
                            initialTabIndex = selectedTabIndex.intValue,
                            onTabChanged = { selectedTabIndex.intValue = it },
                            heightFraction = 0.5f + 0.035f // Cover rounded edges
                        ) { page ->
                            if (page == 0) {
                                AlarmsScreen(
                                    alarms = alarms,
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
                                    onScrolledToEnd = { },
                                    isBottomSheetExpanded = isBottomSheetExpanded
                                )
                            } else {
                                SettingsScreen(
                                    context = requireContext(),
                                )
                            }
                        }
                    }
                }

                if (selectedTabIndex.intValue == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        AnimatedFabMenu(
                            icon = R.drawable.ic_add,
                            text = R.string.title_create,
                            items = listOf(
                                timerItem,
                                watchItem,
                                alarmItem
                            ),
                            onItemClick = { fabItem ->
                                when (fabItem) {
                                    timerItem -> showTimerDialog = true
                                    watchItem -> scheduleWatch()
                                    alarmItem -> showAlarmDialog = true
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                }

                if (showAlarmDialog) {
                    AlarmSchedulerDialog(
                        onDismiss = { showAlarmDialog = false },
                        onTimeSet = { hour, minute ->
                            showAlarmDialog = false
                            scheduleAlarm(hour, minute)
                        }
                    )
                }

                if (showTimerDialog) {
                    TimerSchedulerDialog(
                        onDismiss = { showTimerDialog = false },
                        onTimeChosen = { h, m, s, ring, vibrate ->
                            showTimerDialog = false
                            scheduleTimer(h, m, s, ring, vibrate)
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun AlarmSchedulerDialog(
        onDismiss: () -> Unit,
        onTimeSet: (hour: Int, minute: Int) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        val hourNow = calendar.get(Calendar.HOUR_OF_DAY)
        val minuteNow = calendar.get(Calendar.MINUTE)

        TimeChooserDialog(
            initialHour = hourNow,
            initialMinute = minuteNow,
            is24HourClock = Preferences.MILITARY_TIME.get(LocalContext.current),
            onDismissRequest = onDismiss,
            onTimeSet = onTimeSet
        )
    }

    @Composable
    fun TimerSchedulerDialog(
        onDismiss: () -> Unit,
        onTimeChosen: (hours: Int, minutes: Int, seconds: Int, ringtone: SoundData?, isVibrate: Boolean) -> Unit
    ) {
        TimerFactoryDialog(
            onDismiss = onDismiss,
            onTimeChosen = { h, m, s, ring, vibrate ->
                onTimeChosen(h, m, s, ring, vibrate)
            },
            defaultHours = 0,
            defaultMinutes = 1,
            defaultSeconds = 0
        )
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

        val chronos = context.applicationContext as Chronos
        val timer = chronos.newTimer()
        timer.setDuration(totalMillis, chronos)
        timer.setVibrate(context, isVibrate)
        timer.setSound(context, ringtone)
        timer[chronos] = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        TimerService.startService(context)

        val args = Bundle().apply {
            putParcelable(TimerFragment.EXTRA_TIMER, timer)
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
        val activity = requireActivity()
        val chronosApp = activity.application as Chronos
        val allAlarms = chronosApp.alarms

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
