package com.meenbeese.chronos.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

import androidx.coordinatorlayout.widget.CoordinatorLayout

import com.google.android.material.bottomsheet.BottomSheetBehavior

class ScrollableBottomSheetBehavior<V : View>(
    context: Context, attrs: AttributeSet?
) : BottomSheetBehavior<V>(context, attrs) {

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        val target = findScrollingChild(child)
        return if (target != null && target.canScrollVertically(-1)) {
            false
        } else {
            super.onInterceptTouchEvent(parent, child, event)
        }
    }

    private fun findScrollingChild(view: View): View? {
        if (view.canScrollVertically(-1) || view.canScrollVertically(1)) {
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val scrollingChild = findScrollingChild(child)
                if (scrollingChild != null) {
                    return scrollingChild
                }
            }
        }
        return null
    }
}
