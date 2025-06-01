package com.meenbeese.chronos.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

import com.meenbeese.chronos.interfaces.Subscribable
import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import java.util.Calendar
import java.util.TimeZone

class DigitalClockView : View, OnGlobalLayoutListener, Subscribable {
    private var paint: Paint? = null
    private var timezone: TimeZone? = null
    private val clockScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
        invalidate()
    }

    override fun subscribe() {
        viewTreeObserver.addOnGlobalLayoutListener(this)
        startClock()
    }

    override fun unsubscribe() {
        viewTreeObserver.removeOnGlobalLayoutListener(this)
        stopClock()
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

        val text = FormatUtils.format(context, Calendar.getInstance().time)
        canvas.drawText(
            text,
            width / 2f,
            (height - paint!!.ascent()) / 2,
            paint!!
        )

        TimeZone.setDefault(defaultZone)
    }

    private var updateJob: Job? = null

    private fun startClock() {
        if (updateJob?.isActive == true) return

        updateJob = clockScope.launch {
            while (isActive) {
                invalidate()
                delay(1000)
            }
        }
    }

    private fun stopClock() {
        updateJob?.cancel()
        updateJob = null
    }
}
