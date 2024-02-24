package com.meenbeese.chronos.fragments

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager

import io.reactivex.disposables.Disposable

import jahirfiquitiva.libs.fabsmenu.FABsMenu
import jahirfiquitiva.libs.fabsmenu.FABsMenuListener
import jahirfiquitiva.libs.fabsmenu.TitleFAB

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

import me.jfenn.timedatepickers.dialogs.PickerDialog
import me.jfenn.timedatepickers.dialogs.PickerDialog.OnSelectedListener
import me.jfenn.timedatepickers.views.LinearTimePickerView

import java.util.Calendar
import java.util.TimeZone


class HomeFragment : BaseFragment() {
    private var view: View? = null
    private var timePager: ViewPager? = null
    private var timeIndicator: PageIndicatorView? = null
    private var bottomSheet: View? = null
    private var background: ImageView? = null
    private var overlay: View? = null
    private var menu: FABsMenu? = null
    private var stopwatchFab: TitleFAB? = null
    private var timerFab: TitleFAB? = null
    private var alarmFab: TitleFAB? = null
    private var behavior: BottomSheetBehavior<*>? = null
    private var shouldCollapseBack = false
    private var colorPrimarySubscription: Disposable? = null
    private var colorAccentSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null
    private var textColorPrimaryInverseSubscription: Disposable? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_home, container, false)
        val viewPager = view?.findViewById<ViewPager>(R.id.viewPager)
        val tabLayout = view?.findViewById<TabLayout>(R.id.tabLayout)
        timePager = view?.findViewById(R.id.timePager)
        bottomSheet = view?.findViewById(R.id.bottomSheet)
        timeIndicator = view?.findViewById(R.id.pageIndicator)
        background = view?.findViewById(R.id.background)
        overlay = view?.findViewById(R.id.overlay)
        menu = view?.findViewById(R.id.fabsMenu)
        stopwatchFab = view?.findViewById(R.id.stopwatchFab)
        timerFab = view?.findViewById(R.id.timerFab)
        alarmFab = view?.findViewById(R.id.alarmFab)
        behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior?.isHideable = false
        behavior?.addBottomSheetCallback(object : BottomSheetCallback() {
            private var statusBarHeight = -1
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) bottomSheet.setPadding(
                    0,
                    0,
                    0,
                    0
                ) else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (statusBarHeight < 0) statusBarHeight = requireContext().getStatusBarHeight()
                    bottomSheet.setPadding(0, statusBarHeight, 0, 0)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (statusBarHeight < 0) statusBarHeight = requireContext().getStatusBarHeight()
                bottomSheet.setPadding(0, (slideOffset * statusBarHeight).toInt(), 0, 0)
            }
        })
        val pagerAdapter = SimplePagerAdapter(
            context, childFragmentManager,
            AlarmsFragment.Instantiator(context),
            SettingsFragment.Instantiator(context)
        )
        viewPager?.adapter = pagerAdapter
        tabLayout?.setupWithViewPager(viewPager)
        tabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position > 0) {
                    shouldCollapseBack = behavior?.state != BottomSheetBehavior.STATE_EXPANDED
                    behavior?.state = BottomSheetBehavior.STATE_EXPANDED
                    menu?.hide()
                } else {
                    setClockFragments()
                    menu?.show()
                    if (shouldCollapseBack) {
                        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                        shouldCollapseBack = false
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        setClockFragments()
        view?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                behavior?.peekHeight = view!!.measuredHeight / 2
                view?.findViewById<View>(R.id.timeContainer)?.layoutParams =
                    CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        view!!.measuredHeight / 2
                    )
            }
        })
        colorPrimarySubscription = get()
            .colorPrimary()
            .subscribe { integer: Int? ->
                bottomSheet?.setBackgroundColor(integer!!)
                overlay?.setBackgroundColor(integer!!)
            }
        colorAccentSubscription = get()
            .colorAccent()
            .subscribe { integer: Int? ->
                menu?.menuButtonColor = integer!!
                val color = ContextCompat.getColor(
                    requireContext(),
                    if (chronos!!.activityTheme == Chronos.THEME_AMOLED) R.color.textColorPrimary else R.color.textColorPrimaryNight
                )
                menu?.menuButton?.setColorFilter(color)
                stopwatchFab?.setColorFilter(color)
                timerFab?.setColorFilter(color)
                alarmFab?.setColorFilter(color)
                stopwatchFab?.setBackgroundColor(integer)
                timerFab?.setBackgroundColor(integer)
                alarmFab?.setBackgroundColor(integer)
            }
        textColorPrimarySubscription = get()
            .textColorPrimary()
            .subscribe { integer: Int? ->
                stopwatchFab?.titleTextColor = integer!!
                timerFab?.titleTextColor = integer
                alarmFab?.titleTextColor = integer
            }
        textColorPrimaryInverseSubscription = get()
            .textColorPrimaryInverse()
            .subscribe { integer: Int? ->
                alarmFab?.titleBackgroundColor = integer!!
                stopwatchFab?.titleBackgroundColor = integer
                timerFab?.titleBackgroundColor = integer
            }
        stopwatchFab?.setOnClickListener {
            menu?.collapseImmediately()
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
        timerFab?.setOnClickListener {
            invokeTimerScheduler()
            menu?.collapse()
        }
        alarmFab?.setOnClickListener {
            invokeAlarmScheduler()
            menu?.collapse()
        }
        menu?.menuListener = object : FABsMenuListener() {
            override fun onMenuExpanded(fabsMenu: FABsMenu) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (ContextCompat.checkSelfPermission(
                            context!!,
                            Manifest.permission.FOREGROUND_SERVICE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) requestPermissions(
                        arrayOf(Manifest.permission.FOREGROUND_SERVICE), 0
                    ) else fabsMenu.collapseImmediately()
                }
            }
        }

        // Check actions passed from MainActivity; open timer/alarm schedulers if necessary
        val args = arguments
        val action = args?.getString(INTENT_ACTION, null)
        if (AlarmClock.ACTION_SET_ALARM == action) {
            view?.post { invokeAlarmScheduler() }
        } else if (AlarmClock.ACTION_SET_TIMER == action) {
            view?.post { invokeTimerScheduler() }
        }
        return view
    }

    /**
     * Open the alarm scheduler dialog to allow the user to create
     * a new alarm.
     */
    private fun invokeAlarmScheduler() {
        AestheticTimeSheetPickerDialog(view?.context)
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
        if (timePager != null && timeIndicator != null) {
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
            timePager?.adapter = timeAdapter
            timeIndicator?.setViewPager(timePager!!)
            timeIndicator?.visibility = if (fragments.size > 1) View.VISIBLE else View.GONE
        }
        getBackgroundImage(background!!)
    }

    override fun onDestroyView() {
        colorPrimarySubscription?.dispose()
        colorAccentSubscription?.dispose()
        textColorPrimarySubscription?.dispose()
        textColorPrimaryInverseSubscription?.dispose()
        super.onDestroyView()
    }

    companion object {
        const val INTENT_ACTION = "com.meenbeese.chronos.HomeFragment.INTENT_ACTION"
    }
}
