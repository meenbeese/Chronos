package me.jfenn.alarmio.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;


public class ScrollableBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {

    public ScrollableBottomSheetBehavior() {
        super();
    }

    public ScrollableBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        super.onInterceptTouchEvent(parent, child, event);
        return false;
    }
}
