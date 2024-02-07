package me.jfenn.alarmio.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout


class FragmentLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val tempRect: Rect = Rect()
    private val windowRect: Rect = Rect()

    @Deprecated("Deprecated")
    override fun fitSystemWindows(insets: Rect): Boolean {
        windowRect.set(insets)
        super.fitSystemWindows(insets)
        return false
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        tempRect.set(windowRect)
        super.fitSystemWindows(tempRect)
    }
}
