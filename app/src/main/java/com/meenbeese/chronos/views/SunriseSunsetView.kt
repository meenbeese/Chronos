package com.meenbeese.chronos.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

import androidx.annotation.ColorInt

import com.meenbeese.chronos.utils.AnimFloat

import java.util.Calendar

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


open class SunriseSunsetView : View, OnTouchListener {
    private var sunrisePaint: Paint? = null
    private var sunsetPaint: Paint? = null
    private var linePaint: Paint? = null

    private var dayStart: AnimFloat? = null
    private var dayEnd: AnimFloat? = null

    private var moveBeginStart: Float? = null
    private var moveBeginEnd: Float? = null
    private var min = DAY_HOUR
    private var max = DAY_END

    private var listener: SunriseListener? = null

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
        dayStart = AnimFloat(0.25f)
        dayEnd = AnimFloat(0.75f)

        sunrisePaint = Paint()
        sunrisePaint?.isAntiAlias = true
        sunrisePaint?.style = Paint.Style.FILL
        sunrisePaint?.color = Color.BLACK

        sunsetPaint = Paint()
        sunsetPaint?.isAntiAlias = true
        sunsetPaint?.style = Paint.Style.FILL
        sunsetPaint?.color = Color.BLACK

        linePaint = Paint()
        linePaint?.isAntiAlias = true
        linePaint?.style = Paint.Style.FILL
        linePaint?.color = Color.BLACK
        linePaint?.alpha = 20

        setOnTouchListener(this)
        isClickable = true
        isFocusable = true
    }

    @get:ColorInt
    var sunriseColor: Int
        /**
         * @return The color of the segment of the day where the
         * sun is above the horizon.
         */
        get() = sunrisePaint!!.color
        /**
         * Sets the color for the segment of the day where the
         * sun is above the horizon.
         *
         * @param color                 The sunrise color.
         */
        set(color) {
            sunrisePaint!!.color = color
            postInvalidate()
        }

    @get:ColorInt
    var sunsetColor: Int
        /**
         * @return The color of the segment of the day where the
         * sun is below the horizon.
         */
        get() = sunsetPaint!!.color
        /**
         * Sets the color for the segment of the day where the
         * sun is below the horizon.
         *
         * @param color                 The sunset color.
         */
        set(color) {
            sunsetPaint!!.color = color
            postInvalidate()
        }

    @get:ColorInt
    var futureColor: Int
        /**
         * @return The color of the segment of the day which
         * has yet to occur.
         */
        get() = linePaint!!.color
        /**
         * Sets the color for the future.
         *
         * @param color                 The future color.
         */
        set(color) {
            linePaint!!.color = color
            postInvalidate()
        }

    /**
     * Set the sunrise time, in milliseconds. Values can range
     * beyond the period of a day; they are modulated by a 24 hour
     * period.
     *
     * @param dayStartMillis            The sunrise time, in milliseconds.
     * @param animate                   Whether to animate the change in
     * values.
     */
    fun setSunrise(dayStartMillis: Long, animate: Boolean) {
        val normalizedDayStartMillis = dayStartMillis % DAY_LENGTH
        val normalizedDayStartValue = normalizedDayStartMillis.toFloat() / DAY_LENGTH

        dayStart?.let {
            if (animate) it.targetValue = normalizedDayStartValue
            else it.setCurrentValue(normalizedDayStartValue)
        }

        postInvalidate()
    }

    private var sunrise: Long
        /**
         * Calculate the sunrise time, in milliseconds. Returned values
         * will not range beyond a 24 hour period.
         *
         * @return The sunrise time, in milliseconds.
         */
        get() = (dayStart!!.targetValue * DAY_LENGTH).toLong()
        /**
         * Set the sunrise time, in milliseconds. Values can range
         * beyond the period of a day; they are modulated by a 24 hour
         * period. Change in values will not be animated.
         *
         * @param dayStartMillis            The sunrise time, in milliseconds.
         */
        set(dayStartMillis) {
            setSunrise(dayStartMillis, false)
        }

    /**
     * Set the sunset time, in milliseconds. Values can range
     * beyond the period of a day; they are modulated by a 24 hour
     * period.
     *
     * @param dayEndMillis              The sunset time, in milliseconds.
     * @param animate                   Whether to animate the change in
     * values.
     */
    fun setSunset(dayEndMillis: Long, animate: Boolean) {
        val normalizedDayEndMillis = dayEndMillis % DAY_LENGTH
        val normalizedDayEndValue = normalizedDayEndMillis.toFloat() / DAY_LENGTH

        dayEnd?.let {
            if (animate) it.targetValue = normalizedDayEndValue
            else it.setCurrentValue(normalizedDayEndValue)
        }

        postInvalidate()
    }

    private var sunset: Long
        /**
         * Calculate the sunset time, in milliseconds. Returned values
         * will not range beyond a 24 hour period.
         *
         * @return The sunset time, in milliseconds.
         */
        get() = (dayEnd!!.targetValue * DAY_LENGTH).toLong()
        /**
         * Set the sunset time, in milliseconds. Values can range
         * beyond the period of a day; they are modulated by a 24 hour
         * period. Change in values will not be animated.
         *
         * @param dayEndMillis              The sunset time, in milliseconds.
         */
        set(dayEndMillis) {
            setSunset(dayEndMillis, false)
        }

    /**
     * Specify an interface to receive updates when the sunrise/sunset
     * times are modified by the user. Methods in this interface are only
     * called when the view is interacted with; calling setSunset or
     * setSunrise will not result in this interface being notified.
     *
     * @param listener                  An interface to receive updates
     * when the sunrise/sunset times
     * are modified.
     */
    fun setListener(listener: SunriseListener?) {
        this.listener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        dayStart?.updateValue(true)
        dayEnd?.updateValue(true)

        val scaleX = width / 23f
        val scaleY = height / 2f
        var interval = (dayEnd!!.drawnValue - dayStart!!.drawnValue) / 2
        var interval2 = (1 - dayEnd!!.drawnValue + dayStart!!.drawnValue) / 2
        var start = dayStart!!.drawnValue - (1 - dayEnd!!.drawnValue + dayStart!!.drawnValue)
        interval *= 24 * scaleX
        interval2 *= 24 * scaleX
        start *= 24 * scaleX

        val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]

        val path = Path()
        path.moveTo(start, scaleY)
        path.rQuadTo(interval2, scaleY * ((interval2 / interval + 1) / 2), interval2 * 2, 0f)
        path.rQuadTo(interval, -scaleY * ((interval / interval2 + 1) / 2), interval * 2, 0f)
        path.rQuadTo(interval2, scaleY * ((interval2 / interval + 1) / 2), interval2 * 2, 0f)
        path.rQuadTo(interval, -scaleY * ((interval / interval2 + 1) / 2), interval * 2, 0f)

        canvas.clipPath(path)
        canvas.drawRect(
            0f,
            0f,
            (scaleX.toInt() * hour).toFloat(),
            scaleY.toInt().toFloat(),
            sunrisePaint!!
        )
        canvas.drawRect(
            0f,
            scaleY.toInt().toFloat(),
            (scaleX.toInt() * hour).toFloat(),
            height.toFloat(),
            sunsetPaint!!
        )
        canvas.drawRect(
            (scaleX.toInt() * hour).toFloat(),
            0f,
            width.toFloat(),
            height.toFloat(),
            linePaint!!
        )

        if (!dayStart!!.isTargetValue() || !dayEnd!!.isTargetValue()) postInvalidate()
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val horizontalDistance = event.x / width
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                moveBeginStart = null
                moveBeginEnd = null
                if (abs((horizontalDistance - dayStart!!.drawnValue).toDouble()) < abs((horizontalDistance - dayEnd!!.drawnValue).toDouble())) moveBeginStart =
                    dayStart!!.drawnValue - horizontalDistance
                else moveBeginEnd = dayEnd!!.drawnValue - horizontalDistance
            }

            MotionEvent.ACTION_MOVE -> {
                if (moveBeginStart != null) dayStart!!.targetValue = min(
                    min(
                        (dayEnd!!.targetValue - DAY_HOUR).toDouble(),
                        (dayEnd!!.targetValue - min).toDouble()
                    ),
                    max(
                        max(
                            DAY_START.toDouble(),
                            (dayEnd!!.targetValue - max).toDouble()
                        ), (moveBeginStart!! + horizontalDistance).toDouble()
                    )
                ).toFloat()
                else if (moveBeginEnd != null) dayEnd!!.targetValue = min(
                    min(
                        DAY_END.toDouble(),
                        (dayStart!!.targetValue + max).toDouble()
                    ), max(
                        (dayStart!!.targetValue + DAY_HOUR).toDouble(),
                        max(
                            (moveBeginEnd!! + horizontalDistance).toDouble(),
                            (dayStart!!.targetValue + min).toDouble()
                        )
                    )
                ).toFloat()
                if (parent != null) parent.requestDisallowInterceptTouchEvent(true)
                postInvalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (listener != null) {
                    if (moveBeginStart != null) listener!!.onSunriseChanged(this, sunrise)
                    else if (moveBeginEnd != null) listener!!.onSunsetChanged(this, sunset)
                }
                if (parent != null) parent.requestDisallowInterceptTouchEvent(false)
                moveBeginStart = null
                moveBeginEnd = null
            }
        }
        return false
    }

    interface SunriseListener {
        fun onSunriseChanged(view: SunriseSunsetView?, sunriseMillis: Long)
        fun onSunsetChanged(view: SunriseSunsetView?, sunsetMillis: Long)
    }

    companion object {
        private const val DAY_START = 0f
        private const val DAY_END = 0.99998844f
        private const val DAY_HOUR = 0.04167f
        private const val DAY_LENGTH = 86400000L
    }
}
