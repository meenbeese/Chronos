package com.meenbeese.chronos.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

import androidx.core.content.ContextCompat

import com.meenbeese.chronos.R
import com.meenbeese.chronos.utils.DimenUtils

import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


/**
 * Display a progress circle with text in
 * the center.
 */
class ProgressTextView : View {

    private var progress: Long = 0
    private var maxProgress: Long = 0
    private var referenceProgress: Long = 0
    private var text: String? = null
    private var padding: Int = DimenUtils.dpToPx(4f)

    private val lineColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val circleColor = ContextCompat.getColor(context, R.color.textColorPrimary)
    private val backgroundColor = ContextCompat.getColor(context, R.color.colorIndeterminateText)
    private val textColorPrimary = ContextCompat.getColor(context, R.color.colorIndeterminateText)

    private var linePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = padding.toFloat()
        color = lineColor
    }

    private var circlePaint = Paint().apply {
        isAntiAlias = true
        color = circleColor
    }

    private var backgroundPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = padding.toFloat()
        color = backgroundColor
    }

    private var textPaint = Paint().apply {
        color = textColorPrimary
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = DimenUtils.spToPx(34f).toFloat()
        isFakeBoldText = true
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * Set the text (time) to display in the center
     * of the view.
     */
    fun setText(text: String) {
        this.text = text
        invalidate()
    }

    /**
     * Set the current progress value.
     */
    @JvmOverloads
    fun setProgress(progress: Long, animate: Boolean = false) {
        if (animate) {
            ValueAnimator.ofFloat(this.progress.toFloat(), progress.toFloat()).apply {
                interpolator = LinearInterpolator()
                addUpdateListener { valueAnimator ->
                    (valueAnimator.animatedValue as? Float)?.toLong()?.let { value ->
                        setProgress(value, false)
                    }
                }
                start()
            }
        } else {
            this.progress = progress
            postInvalidate()
        }
    }

    /**
     * Set the largest progress that has been acquired so far.
     */
    @JvmOverloads
    fun setMaxProgress(maxProgress: Long, animate: Boolean = false) {
        if (animate) {
            ValueAnimator.ofFloat(this.maxProgress.toFloat(), maxProgress.toFloat()).apply {
                interpolator = LinearInterpolator()
                addUpdateListener { valueAnimator ->
                    (valueAnimator.animatedValue as? Float)?.toLong()?.let { value ->
                        setMaxProgress(value, false)
                    }
                }
                start()
            }
        } else {
            this.maxProgress = maxProgress
            postInvalidate()
        }
    }

    /**
     * Set the progress value of the reference dot (?) on
     * the circle. Mostly used in the stopwatch, to indicate
     * the previous/best lap time.
     */
    fun setReferenceProgress(referenceProgress: Long) {
        this.referenceProgress = referenceProgress
        postInvalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth = (width * 1.0).toInt()
        val desiredHeight = (height * 0.9).toInt()
        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        val size = min(width, height)
        val sidePadding = padding * 3
        val radius = (size / 2 - sidePadding).toFloat()
        val rectF = RectF(
            (width / 2 - radius),
            (height / 2 - radius),
            (width / 2 + radius),
            (height / 2 + radius)
        )

        if (maxProgress > 0) {
            val sweepAngle = 360f * progress / maxProgress

            // Draw remaining arc (gray)
            canvas.drawArc(rectF, -90f + sweepAngle, 360f - sweepAngle, false, backgroundPaint)

            // Draw progress arc (blue)
            canvas.drawArc(rectF, -90f, sweepAngle, false, linePaint)

            val progressX = width / 2 + cos((sweepAngle - 90) * Math.PI / 180).toFloat() * radius
            val progressY = height / 2 + sin((sweepAngle - 90) * Math.PI / 180).toFloat() * radius

            canvas.drawCircle(progressX, progressY, (2 * padding).toFloat(), circlePaint)
        }

        text?.let { str ->
            val textBounds = Rect()
            textPaint.getTextBounds(str, 0, str.length, textBounds)

            val xPos = (width / 2).toFloat()
            val yPos = (height / 2).toFloat()

            canvas.drawText(str, xPos, yPos, textPaint)
        }
    }
}
