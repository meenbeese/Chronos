package com.meenbeese.chronos.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

import com.meenbeese.chronos.interfaces.Subscribable
import com.meenbeese.chronos.utils.FormatUtils

import java.util.Calendar
import java.util.TimeZone

class DigitalClockView : View, OnGlobalLayoutListener, Subscribable {
    private var paint: Paint? = null
    private var thread: UpdateThread? = null
    private var timezone: TimeZone? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        timezone = TimeZone.getDefault()
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.color = Color.BLACK
        paint!!.textAlign = Paint.Align.CENTER
    }

    fun setTimezone(timezone: String) {
        this.timezone = TimeZone.getTimeZone(timezone)
    }

    override fun subscribe() {
        viewTreeObserver.addOnGlobalLayoutListener(this)
        if (thread == null) thread = UpdateThread(this)
        thread?.start()
    }

    override fun unsubscribe() {
        viewTreeObserver.removeOnGlobalLayoutListener(this)
        thread?.interrupt()
        thread = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        subscribe()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unsubscribe()
    }

    override fun onGlobalLayout() {
        paint?.textSize = 48f
        val bounds = Rect()
        paint?.getTextBounds("00:00:00", 0, 8, bounds)
        paint?.textSize = 48f * measuredWidth / (bounds.width() * 2)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val defaultZone = TimeZone.getDefault()
        TimeZone.setDefault(timezone)
        canvas.drawText(
            FormatUtils.format(context, Calendar.getInstance().time),
            width.toFloat() / 2,
            (height - paint!!.ascent()) / 2,
            paint!!
        )
        TimeZone.setDefault(defaultZone)
    }

    private class UpdateThread(private val view: DigitalClockView) : Thread() {
        override fun run() {
            while (true) {
                try {
                    sleep(1000)
                } catch (e: InterruptedException) {
                    return
                }
                Handler(Looper.getMainLooper()).post {
                    view.invalidate()
                }
            }
        }
    }
}
