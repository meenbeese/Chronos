package com.meenbeese.chronos.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import androidx.coordinatorlayout.widget.CoordinatorLayout

import com.google.android.material.bottomsheet.BottomSheetBehavior


class ScrollableBottomSheetBehavior<V : View>(context: Context?, attrs: AttributeSet?) :
    BottomSheetBehavior<V>(
        context!!, attrs
    ) {
    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        super.onInterceptTouchEvent(parent, child, event)
        return false
    }
}
