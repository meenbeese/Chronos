package com.meenbeese.chronos.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.SimplePagerAdapter
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.dialogs.TimerFactoryDialog
import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.toEntity
import com.meenbeese.chronos.databinding.FragmentHomeBinding
import com.meenbeese.chronos.db.AlarmViewModel
import com.meenbeese.chronos.db.AlarmViewModelFactory
import com.meenbeese.chronos.dialogs.TimeChooserDialog
import com.meenbeese.chronos.interfaces.AlarmNavigator
import com.meenbeese.chronos.screens.ClockScreen
import com.meenbeese.chronos.services.TimerService
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.utils.ImageUtils.getContrastingTextColorFromBg
import com.meenbeese.chronos.utils.ImageUtils.rememberBackgroundPainterState
import com.meenbeese.chronos.views.AnimatedFabMenu
import com.meenbeese.chronos.views.ClockPageView
import com.meenbeese.chronos.views.CustomTabView
import com.meenbeese.chronos.views.FabItem

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var behavior: BottomSheetBehavior<*>
    private lateinit var alarmViewModel: AlarmViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomSheet) { v, insets ->
            val statusBarInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setTag(R.id.viewPager, statusBarInset)
            insets
        }

        val app = requireActivity().application as Chronos
        val factory = AlarmViewModelFactory(app.repository)
        alarmViewModel = ViewModelProvider(this, factory)[AlarmViewModel::class.java]
        alarmViewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            Log.d("HomeFragment", "Alarms updated, size: ${alarms.size}")
            if (alarms.isEmpty() && behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        val tabView = binding.root.findViewById<ComposeView>(R.id.tabLayoutCompose)

        val tabs = listOf("Alarms", "Settings")
        val selectedTabIndex = mutableIntStateOf(0)

        tabView.setContent {
            CustomTabView(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex.intValue,
                onTabSelected = { index ->
                    selectedTabIndex.intValue = index
                    binding.viewPager.currentItem = index

                    if (index == 0) {
                        behavior.isDraggable = true
                        behavior.peekHeight = binding.bottomSheet.measuredHeight / 2
                        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    } else {
                        behavior.isDraggable = false
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            )
        }

        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.isHideable = false
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val statusBarHeight = bottomSheet.getTag(R.id.viewPager) as? Int ?: 0

                _binding?.let { binding ->
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            bottomSheet.setPadding(0, 0, 0, 0)
                            bottomSheet.elevation = 8f
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            bottomSheet.setPadding(0, statusBarHeight, 0, 0)
                            bottomSheet.elevation = 16f
                        }
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                _binding?.let { binding ->
                    val statusBarHeight = bottomSheet.getTag(R.id.viewPager) as? Int ?: 0

                    val easedOffset = slideOffset.coerceIn(0f, 1f).let {
                        (1 - cos(it * PI)) / 2.0
                    }

                    bottomSheet.setPadding(0, (easedOffset * statusBarHeight).toInt(), 0, 0)
                }
            }
        })

        val pagerAdapter = SimplePagerAdapter(
            this,
            AlarmsFragment.Instantiator(context),
            SettingsFragment.Instantiator(context)
        )

        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectedTabIndex.intValue = position

                if (position == 0) {
                    binding.speedDial.apply {
                        visibility = View.VISIBLE
                        disposeComposition()
                        post { setupSpeedDial() }
                    }
                    behavior.isDraggable = true
                    behavior.peekHeight = binding.bottomSheet.measuredHeight / 2
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    binding.speedDial.apply {
                        disposeComposition()
                        visibility = View.INVISIBLE
                    }
                    behavior.isDraggable = false
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
        binding.viewPager.post {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                attachScrollListenerToAlarms()
            }
        }

        setupSpeedDial()
        setClockFragments()

        handleIntentActions()

        return binding.root
    }

    private fun setupSpeedDial() {
        binding.speedDial.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
        )
        binding.speedDial.setContent {
            val timerItem = FabItem(icon = R.drawable.ic_timer, text = R.string.title_set_timer)
            val watchItem = FabItem(icon = R.drawable.ic_stopwatch, text = R.string.title_set_stopwatch)
            val alarmItem = FabItem(icon = R.drawable.ic_alarm_add, text = R.string.title_set_alarm)

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
                        timerItem -> invokeTimerScheduler()
                        watchItem -> {
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
                        alarmItem -> invokeAlarmScheduler()
                    }
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        _binding?.let { binding ->
            if (binding.viewPager.currentItem == 1) {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
            } else {
                behavior.isDraggable = true
            }
        }
    }

    private fun attachScrollListenerToAlarms() {
        val alarmsFragment = childFragmentManager.findFragmentByTag("f0") as? AlarmsFragment
        val recyclerView = alarmsFragment?.recyclerView ?: return

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var lastStateChangeTime = 0L
            private val debounceInterval = 300L

            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val currentTime = System.currentTimeMillis()
                val threshold = 10

                if (abs(dy) < threshold) return

                if (dy > 0 && behavior.state != BottomSheetBehavior.STATE_EXPANDED && rv.canScrollVertically(1).not()) {
                    if (currentTime - lastStateChangeTime > debounceInterval) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        lastStateChangeTime = currentTime
                    }
                } else if (dy < 0 && behavior.state != BottomSheetBehavior.STATE_COLLAPSED && rv.canScrollVertically(-1).not()) {
                    if (currentTime - lastStateChangeTime > debounceInterval) {
                        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        lastStateChangeTime = currentTime
                    }
                }
            }
        })
    }

    /**
     * Check actions passed from MainActivity; open timer/alarm
     * schedulers if necessary.
     */
    private fun handleIntentActions() {
        val action = arguments?.getString(INTENT_ACTION)
        when (action) {
            AlarmClock.ACTION_SET_ALARM -> binding.root.post { invokeAlarmScheduler() }
            AlarmClock.ACTION_SET_TIMER -> binding.root.post { invokeTimerScheduler() }
        }
    }

    /**
     * Open the alarm scheduler dialog to allow the user to create
     * a new alarm.
     */
    private fun invokeAlarmScheduler() {
        val calendar = Calendar.getInstance()
        val hourNow = calendar.get(Calendar.HOUR_OF_DAY)
        val minuteNow = calendar.get(Calendar.MINUTE)

        val composeView = binding.root.findViewById<ComposeView>(R.id.composeDialogHost)

        composeView.disposeComposition()
        composeView.setContent {
            var showDialog by remember { mutableStateOf(true) }

            if (showDialog) {
                TimeChooserDialog(
                    initialHour = hourNow,
                    initialMinute = minuteNow,
                    is24HourClock = Preferences.MILITARY_TIME.get(requireContext()),
                    onDismissRequest = { showDialog = false },
                    onTimeSet = { hour, minute ->
                        showDialog = false

                        val time = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)

                            if (BuildConfig.DEBUG) {
                                add(Calendar.MINUTE, 1)
                            }
                        }.timeInMillis

                        val alarm = AlarmData(
                            id = 0,
                            name = null,
                            time = Calendar.getInstance().apply { timeInMillis = time },
                            isEnabled = true,
                            days = MutableList(7) { false }, // All days off initially
                            isVibrate = true,
                            sound = null
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            val entity = alarm.toEntity()
                            val id = alarmViewModel.insertAndReturnId(entity)

                            alarm.id = id.toInt()
                            alarm.set(requireContext())
                        }

                        val formattedTime = FormatUtils.formatShort(context, Date(time))
                        Toast.makeText(requireContext(), "Alarm set for $formattedTime", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    /**
     * Open the timer scheduler dialog to allow the user to start
     * a timer.
     */
    private fun invokeTimerScheduler() {
        val context = requireContext()
        val manager = parentFragmentManager
        val chronos = context.applicationContext as Chronos
        val composeView = binding.root.findViewById<ComposeView>(R.id.composeDialogHost2)

        composeView.disposeComposition()
        composeView.setContent {
            var showDialog by remember { mutableStateOf(true) }

            if (showDialog) {
                TimerFactoryDialog(
                    onDismiss = { showDialog = false },
                    onTimeChosen = { hours, minutes, seconds, ringtone, isVibrate ->
                        showDialog = false

                        val totalMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L

                        if (totalMillis <= 0) {
                            Toast.makeText(requireContext(), "Invalid timer duration", Toast.LENGTH_SHORT).show()
                            return@TimerFactoryDialog
                        }

                        val timer = chronos.newTimer()
                        timer.setDuration(totalMillis, chronos)
                        timer.setVibrate(context, isVibrate)
                        timer.setSound(context, ringtone)
                        timer[chronos] = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                        TimerService.startService(context)

                        val args = Bundle().apply {
                            putParcelable(TimerFragment.EXTRA_TIMER, timer)
                        }

                        val fragment = TimerFragment().apply {
                            arguments = args
                        }

                        manager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_up_sheet,
                                R.anim.slide_out_up_sheet,
                                R.anim.slide_in_down_sheet,
                                R.anim.slide_out_down_sheet
                            )
                            .replace(R.id.fragment, fragment)
                            .addToBackStack(null)
                            .commit()
                    },
                    defaultHours = 0,
                    defaultMinutes = 1,
                    defaultSeconds = 0
                )
            }
        }
    }

    /**
     * Update the time zones displayed in the clock fragments pager.
     */
    internal fun setClockFragments() {
        val fragments = mutableListOf<@Composable () -> Unit>()
        val timeZones = mutableListOf(TimeZone.getDefault().id)

        if (Preferences.TIME_ZONE_ENABLED.get(requireContext())) {
            val rawCsv = Preferences.TIME_ZONES.get(requireContext())
            val selectedIds = rawCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            for (id in selectedIds) {
                if (TimeZone.getAvailableIDs().contains(id)) {
                    timeZones.add(id)
                }
            }
        }

        for (timeZoneId in timeZones) {
            fragments.add {
                ClockScreen(
                    timezoneId = timeZoneId,
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

        binding.clockPageView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.clockPageView.setContent {
            val background = rememberBackgroundPainterState(isAlarm = false)

            ClockPageView(
                fragments = fragments,
                backgroundPainter = background!!,
                pageIndicatorVisible = fragments.size > 1
            )
        }
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
