package com.meenbeese.chronos.fragments

import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.SimplePagerAdapter
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.dialogs.TimerDialog
import com.meenbeese.chronos.utils.DimenUtils.getStatusBarHeight
import com.meenbeese.chronos.utils.ImageUtils.getBackgroundImage
import com.meenbeese.chronos.views.PageIndicatorView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView

import java.util.Calendar
import java.util.TimeZone

class HomeFragment : BaseFragment() {
    private lateinit var view: View
    private lateinit var timePager: ViewPager2
    private lateinit var timeIndicator: PageIndicatorView
    private lateinit var bottomSheet: View
    private lateinit var background: ImageView
    private lateinit var overlay: View
    private lateinit var speedDialView: SpeedDialView
    private lateinit var behavior: BottomSheetBehavior<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_home, container, false)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        timePager = view.findViewById(R.id.timePager)
        bottomSheet = view.findViewById(R.id.bottomSheet)
        timeIndicator = view.findViewById(R.id.pageIndicator)
        background = view.findViewById(R.id.background)
        overlay = view.findViewById(R.id.overlay)
        speedDialView = view.findViewById(R.id.speedDial)

        behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.isHideable = false
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            private var statusBarHeight = -1
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                speedDialView.close()
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheet.setPadding(0, 0, 0, 0)
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (statusBarHeight < 0) statusBarHeight = requireContext().getStatusBarHeight()
                    bottomSheet.setPadding(0, statusBarHeight, 0, 0)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                speedDialView.close()
                if (statusBarHeight < 0) statusBarHeight = requireContext().getStatusBarHeight()
                bottomSheet.setPadding(0, (slideOffset * statusBarHeight).toInt(), 0, 0)
            }
        })

        val pagerAdapter = SimplePagerAdapter(
            this,
            AlarmsFragment.Instantiator(context),
            SettingsFragment.Instantiator(context)
        )
        viewPager.adapter = pagerAdapter
        viewPager.post { attachScrollListenerToAlarms() }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = pagerAdapter.getTitle(position)
        }.attach()

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    speedDialView.show()
                    behavior.isDraggable = true
                    behavior.peekHeight = view.measuredHeight / 2
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    speedDialView.hide()
                    behavior.isDraggable = false
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        tabLayout.setOnTouchListener(object : View.OnTouchListener {
            private var initialY = 0f
            private var isDragging = false
            private var initialX = 0f
            private val clickThreshold = 10

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event ?: return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialY = event.rawY
                        initialX = event.rawX
                        isDragging = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dy = initialY - event.rawY
                        val dx = initialX - event.rawX
                        if (dy > 30) {
                            if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                            }
                            isDragging = true
                            return true
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val dy = Math.abs(event.rawY - initialY)
                        val dx = Math.abs(event.rawX - initialX)
                        if (!isDragging && dy < clickThreshold && dx < clickThreshold) {
                            v?.performClick()
                            return false
                        }
                        if (isDragging) {
                            return true
                        }
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        if (isDragging) {
                            return true
                        }
                    }
                }
                return false
            }
        })

        setSpeedDialView()
        setClockFragments()

        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val halfHeight = view.measuredHeight / 2
                behavior.peekHeight = halfHeight

                behavior.state = BottomSheetBehavior.STATE_COLLAPSED

                behavior.isDraggable = true

                view.findViewById<View>(R.id.timeContainer)?.layoutParams =
                    CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        halfHeight
                    )
            }
        })

        handleIntentActions()

        return view
    }

    private fun setSpeedDialView() {
        speedDialView.addActionItem(
            SpeedDialActionItem
                .Builder(R.id.alarm_fab, R.drawable.ic_alarm_add)
                .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                .setLabelColor(ContextCompat.getColor(requireContext(), R.color.textColorSecondary))
                .setLabel(R.string.title_set_alarm)
                .setLabelClickable(true)
                .create()
        )
        speedDialView.addActionItem(
            SpeedDialActionItem
                .Builder(R.id.timer_fab, R.drawable.ic_timer)
                .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                .setLabelColor(ContextCompat.getColor(requireContext(), R.color.textColorSecondary))
                .setLabel(R.string.title_set_timer)
                .setLabelClickable(true)
                .create()
        )
        speedDialView.addActionItem(
            SpeedDialActionItem
                .Builder(R.id.stopwatch_fab, R.drawable.ic_stopwatch)
                .setFabBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                .setFabImageTintColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                .setLabelColor(ContextCompat.getColor(requireContext(), R.color.textColorSecondary))
                .setLabel(R.string.title_set_stopwatch)
                .setLabelClickable(true)
                .create()
        )
        speedDialView.setOnActionSelectedListener { actionItem ->
            speedDialView.close()
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
                else -> speedDialView.hide()
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
                val canScrollDown = rv.canScrollVertically(1)
                val canScrollUp = rv.canScrollVertically(-1)

                if (!canScrollDown && dy > 0 && behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    if (currentTime - lastStateChangeTime > debounceInterval) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        lastStateChangeTime = currentTime
                    }
                }

                if (!canScrollUp && dy < 0 && behavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
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
        val args = arguments
        val action = args?.getString(INTENT_ACTION, null)
        if (AlarmClock.ACTION_SET_ALARM == action) {
            view.post { invokeAlarmScheduler() }
        } else if (AlarmClock.ACTION_SET_TIMER == action) {
            view.post { invokeTimerScheduler() }
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

        val timeChooserDialog = android.app.TimePickerDialog(context, { _, hour, minute ->
            val alarm = AlarmData(
                id = 0,
                time = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }
            )

            alarm.saveToDatabase(requireContext())
            alarm.set(requireContext())
        }, hourNow, minuteNow, true)

        timeChooserDialog.show()
    }

    /**
     * Open the timer scheduler dialog to allow the user to start
     * a timer.
     */
    private fun invokeTimerScheduler() {
        TimerDialog(requireContext(), parentFragmentManager)
            .show()
    }

    /**
     * Update the time zones displayed in the clock fragments pager.
     */
    private fun setClockFragments() {
        val fragments = mutableListOf<ClockFragment.Instantiator>()

        fragments.add(ClockFragment.Instantiator(context, null))

        for (id in TimeZone.getAvailableIDs()) {
            if (PreferenceData.TIME_ZONE_ENABLED.getValue<Boolean>(requireContext())) {
                fragments.add(ClockFragment.Instantiator(context, id))
            }
        }

        val timeAdapter = SimplePagerAdapter(this, *fragments.toTypedArray())
        timePager.adapter = timeAdapter

        timeIndicator.setViewPager(timePager)

        timeIndicator.visibility = if (fragments.size > 1) View.VISIBLE else View.GONE

        getBackgroundImage(background)
    }

    companion object {
        const val INTENT_ACTION = "com.meenbeese.chronos.HomeFragment.INTENT_ACTION"
    }
}
