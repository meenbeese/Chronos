package com.meenbeese.chronos.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.leinardi.android.speeddial.SpeedDialView

class CustomTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TabLayout(context, attrs) {

    private var speedDialView: SpeedDialView? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var parentView: View? = null

    private var initialY = 0f
    private var initialX = 0f
    private var isDragging = false
    private val clickThreshold = 10

    fun setup(
        speedDial: SpeedDialView,
        behavior: BottomSheetBehavior<*>,
        containerView: View
    ) {
        speedDialView = speedDial
        bottomSheetBehavior = behavior
        parentView = containerView

        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                if (tab?.position == 0) {
                    speedDialView?.show()
                    bottomSheetBehavior?.isDraggable = true
                    parentView?.let {
                        bottomSheetBehavior?.peekHeight = it.measuredHeight / 2
                    }
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    speedDialView?.hide()
                    bottomSheetBehavior?.isDraggable = false
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onTabUnselected(tab: Tab?) {}
            override fun onTabReselected(tab: Tab?) {}
        })

        setOnTouchListener { v, event ->
            event ?: return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.rawY
                    initialX = event.rawX
                    isDragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = initialY - event.rawY
                    if (dy > 30) {
                        if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
                            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        isDragging = true
                        return@setOnTouchListener true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val dy = kotlin.math.abs(event.rawY - initialY)
                    val dx = kotlin.math.abs(event.rawX - initialX)
                    if (!isDragging && dy < clickThreshold && dx < clickThreshold) {
                        v?.performClick()
                        return@setOnTouchListener false
                    }
                    if (isDragging) {
                        return@setOnTouchListener true
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }
}
