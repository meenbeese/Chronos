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
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.leinardi.android.speeddial.SpeedDialActionItem
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
import com.meenbeese.chronos.services.TimerService
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.views.CustomTabView

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

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectedTabIndex.intValue = position
            }
        })

        tabView.setContent {
            CustomTabView(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex.intValue,
                onTabSelected = { index ->
                    selectedTabIndex.intValue = index
                    binding.viewPager.currentItem = index

                    if (index == 0) {
                        binding.speedDial.show()
                        behavior.isDraggable = true
                        behavior.peekHeight = binding.bottomSheet.measuredHeight / 2
                        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    } else {
                        binding.speedDial.hide()
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
                    binding.speedDial.close()
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
                    binding.speedDial.close()
                    binding.speedDial.alpha = 1f - slideOffset
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
                super.onPageSelected(position)
                val title = pagerAdapter.getTitle(position)
                if (title == getString(R.string.title_settings)) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
        binding.viewPager.post {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                attachScrollListenerToAlarms()
            }
        }

        setSpeedDialView()
        setClockFragments()

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val safeBinding = _binding ?: return
                safeBinding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val halfHeight = safeBinding.root.measuredHeight / 2
                behavior.peekHeight = halfHeight
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                behavior.isDraggable = true

                safeBinding.timeContainer.layoutParams.height = halfHeight
                safeBinding.timeContainer.requestLayout()
            }
        })

        handleIntentActions()

        return binding.root
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

    private fun setSpeedDialView() {
        val context = requireContext()

        val primary = ContextCompat.getColor(context, R.color.md_theme_primary)
        val onPrimary = ContextCompat.getColor(context, R.color.md_theme_onPrimary)
        val surface = ContextCompat.getColor(context, R.color.md_theme_surface)
        val onSurface = ContextCompat.getColor(context, R.color.md_theme_onSurface)

        binding.speedDial.mainFabOpenedBackgroundColor = primary
        binding.speedDial.mainFabClosedBackgroundColor = primary
        binding.speedDial.addActionItem(
            SpeedDialActionItem
                .Builder(R.id.alarm_fab, R.drawable.ic_alarm_add)
                .setFabBackgroundColor(primary)
                .setFabImageTintColor(onPrimary)
                .setLabelColor(onSurface)
                .setLabelBackgroundColor(surface)
                .setLabel(R.string.title_set_alarm)
                .setLabelClickable(true)
                .create()
        )
        binding.speedDial.addActionItem(
            SpeedDialActionItem
                .Builder(R.id.timer_fab, R.drawable.ic_timer)
                .setFabBackgroundColor(primary)
                .setFabImageTintColor(onPrimary)
                .setLabelColor(onSurface)
                .setLabelBackgroundColor(surface)
                .setLabel(R.string.title_set_timer)
                .setLabelClickable(true)
                .create()
        )
        binding.speedDial.addActionItem(
            SpeedDialActionItem
                .Builder(R.id.stopwatch_fab, R.drawable.ic_stopwatch)
                .setFabBackgroundColor(primary)
                .setFabImageTintColor(onPrimary)
                .setLabelColor(onSurface)
                .setLabelBackgroundColor(surface)
                .setLabel(R.string.title_set_stopwatch)
                .setLabelClickable(true)
                .create()
        )
        binding.speedDial.setOnActionSelectedListener { actionItem ->
            binding.speedDial.close()
            when (actionItem.id) {
                R.id.alarm_fab -> invokeAlarmScheduler()
                R.id.timer_fab -> invokeTimerScheduler()
                R.id.stopwatch_fab -> {
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
                else -> binding.speedDial.hide()
            }
            false
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
        val fragments = mutableListOf<ClockFragment.Instantiator>()

        // Always add the default/local time zone clock
        fragments.add(ClockFragment.Instantiator(context, null))

        // Check if displaying additional time zones is enabled
        if (Preferences.TIME_ZONE_ENABLED.get(requireContext())) {
            val rawCsv = Preferences.TIME_ZONES.get(requireContext())
            val selectedIds = rawCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            for (id in selectedIds) {
                if (TimeZone.getAvailableIDs().contains(id)) {
                    fragments.add(ClockFragment.Instantiator(context, id))
                }
            }
        }

        val timeAdapter = SimplePagerAdapter(this, *fragments.toTypedArray())
        binding.timePager.adapter = timeAdapter
        binding.pageIndicator.setViewPager(binding.timePager)
        binding.pageIndicator.visibility = if (fragments.size > 1) View.VISIBLE else View.GONE
    }

    companion object {
        const val INTENT_ACTION = "com.meenbeese.chronos.HomeFragment.INTENT_ACTION"
    }
}
