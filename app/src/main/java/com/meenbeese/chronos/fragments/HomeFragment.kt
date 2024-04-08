package com.meenbeese.chronos.fragments

import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewpager.widget.ViewPager

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.SimplePagerAdapter
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.dialogs.AestheticTimeSheetPickerDialog
import com.meenbeese.chronos.dialogs.TimerDialog
import com.meenbeese.chronos.interfaces.FragmentInstantiator
import com.meenbeese.chronos.utils.DimenUtils.getStatusBarHeight
import com.meenbeese.chronos.utils.ImageUtils.getBackgroundImage
import com.meenbeese.chronos.views.PageIndicatorView
import com.afollestad.aesthetic.Aesthetic.Companion.get
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.leinardi.android.speeddial.SpeedDialView

import me.jfenn.timedatepickers.dialogs.PickerDialog
import me.jfenn.timedatepickers.dialogs.PickerDialog.OnSelectedListener
import me.jfenn.timedatepickers.views.LinearTimePickerView

import java.util.Calendar
import java.util.TimeZone


class HomeFragment : BaseFragment() {
    private lateinit var view: View
    private lateinit var timePager: ViewPager
    private lateinit var timeIndicator: PageIndicatorView
    private lateinit var bottomSheet: View
    private lateinit var background: ImageView
    private lateinit var overlay: View
    private lateinit var speedDialView: SpeedDialView
    private lateinit var behavior: BottomSheetBehavior<*>
    private var shouldCollapseBack = false
    private var colorPrimarySubscription: Disposable? = null
    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_home, container, false)
        val viewPager = view.findViewById<ViewPager>(R.id.viewPager)
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
                if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    bottomSheet.setPadding(0, 0, 0, 0)
                else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
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
            context, childFragmentManager,
            AlarmsFragment.Instantiator(context),
            SettingsFragment.Instantiator(context)
        )
        speedDialView.inflate(R.menu.fab)
        viewPager?.adapter = pagerAdapter
        tabLayout?.setupWithViewPager(viewPager)
        tabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position > 0) {
                    speedDialView.hide()
                    shouldCollapseBack = behavior.state != BottomSheetBehavior.STATE_EXPANDED
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                } else {
                    setClockFragments()
                    speedDialView.show()
                    if (shouldCollapseBack) {
                        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        shouldCollapseBack = false
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        setClockFragments()
        view.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                behavior.peekHeight = view.measuredHeight / 2
                view.findViewById<View>(R.id.timeContainer)?.layoutParams =
                    CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        view.measuredHeight / 2
                    )
            }
        })
        colorPrimarySubscription = get()
            .colorPrimary()
            .subscribeBy(
                onNext = { integer: Int ->
                    speedDialView.updateColors()
                    bottomSheet.setBackgroundColor(integer)
                    overlay.setBackgroundColor(integer)
                },
                onError = { it.printStackTrace() }
            ).also { disposables.add(it) }

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

        // Check actions passed from MainActivity; open timer/alarm schedulers if necessary
        val args = arguments
        val action = args?.getString(INTENT_ACTION, null)
        if (AlarmClock.ACTION_SET_ALARM == action) {
            view.post { invokeAlarmScheduler() }
        } else if (AlarmClock.ACTION_SET_TIMER == action) {
            view.post { invokeTimerScheduler() }
        }
        return view
    }

    /**
     * Open the alarm scheduler dialog to allow the user to create
     * a new alarm.
     */
    private fun invokeAlarmScheduler() {
        AestheticTimeSheetPickerDialog(view.context)
            .setListener(object : OnSelectedListener<LinearTimePickerView> {
                override fun onSelect(
                    dialog: PickerDialog<LinearTimePickerView>,
                    view: LinearTimePickerView
                ) {
                    val manager = view.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val alarm = chronos!!.newAlarm()
                    alarm.time[Calendar.HOUR_OF_DAY] = view.hourOfDay
                    alarm.time[Calendar.MINUTE] = view.minute
                    alarm.setTime(chronos, manager, alarm.time.timeInMillis)
                    alarm.setEnabled(context, manager, true)
                    chronos?.onAlarmsChanged()
                }

                override fun onCancel(dialog: PickerDialog<LinearTimePickerView>) {}
            })
            .show()
    }

    /**
     * Helper extension function to manage the colors of the FAB.
     */
    private fun SpeedDialView.updateColors() {
        when (chronos?.activityTheme) {
            Chronos.THEME_DAY -> {
                speedDialView.mainFabOpenedIconColor = resources.getColor(R.color.textColorPrimaryNight, chronos?.theme)
                speedDialView.mainFabClosedIconColor = resources.getColor(R.color.textColorPrimaryNight, chronos?.theme)
                speedDialView.mainFabOpenedBackgroundColor = resources.getColor(R.color.colorAccent, chronos?.theme)
                speedDialView.mainFabClosedBackgroundColor = resources.getColor(R.color.colorAccent, chronos?.theme)
            }
            Chronos.THEME_NIGHT -> {
                speedDialView.mainFabOpenedIconColor = resources.getColor(R.color.textColorPrimaryNight, chronos?.theme)
                speedDialView.mainFabClosedIconColor = resources.getColor(R.color.textColorPrimaryNight, chronos?.theme)
                speedDialView.mainFabOpenedBackgroundColor = resources.getColor(R.color.colorNightAccent, chronos?.theme)
                speedDialView.mainFabClosedBackgroundColor = resources.getColor(R.color.colorNightAccent, chronos?.theme)
            }
            Chronos.THEME_DAY_NIGHT -> {
                speedDialView.mainFabOpenedIconColor = resources.getColor(R.color.textColorPrimaryNight, chronos?.theme)
                speedDialView.mainFabClosedIconColor = resources.getColor(R.color.textColorPrimaryNight, chronos?.theme)
                speedDialView.mainFabOpenedBackgroundColor = resources.getColor(R.color.colorPrimary, chronos?.theme)
                speedDialView.mainFabClosedBackgroundColor = resources.getColor(R.color.colorPrimary, chronos?.theme)
            }
            Chronos.THEME_AMOLED -> {
                speedDialView.mainFabOpenedIconColor = resources.getColor(R.color.textColorPrimary, chronos?.theme)
                speedDialView.mainFabClosedIconColor = resources.getColor(R.color.textColorPrimary, chronos?.theme)
                speedDialView.mainFabOpenedBackgroundColor = resources.getColor(R.color.textColorPrimaryNight, chronos?.theme)
                speedDialView.mainFabClosedBackgroundColor = resources.getColor(R.color.textColorPrimaryNight, chronos?.theme)
            }
            else -> return
        }
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
        val fragments: MutableList<FragmentInstantiator> = ArrayList()
        fragments.add(ClockFragment.Instantiator(context, null))
        for (id in TimeZone.getAvailableIDs()) {
            if (PreferenceData.TIME_ZONE_ENABLED.getSpecificValue(context, id)) fragments.add(
                ClockFragment.Instantiator(
                    context, id
                )
            )
        }
        val timeAdapter = SimplePagerAdapter(
            context,
            childFragmentManager,
            *fragments.toTypedArray<FragmentInstantiator>()
        )
        timePager.adapter = timeAdapter
        timeIndicator.setViewPager(timePager)
        timeIndicator.visibility = if (fragments.size > 1) View.VISIBLE else View.GONE
        getBackgroundImage(background)
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    companion object {
        const val INTENT_ACTION = "com.meenbeese.chronos.HomeFragment.INTENT_ACTION"
    }
}
