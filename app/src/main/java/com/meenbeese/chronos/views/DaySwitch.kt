package com.meenbeese.chronos.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator

import com.meenbeese.chronos.utils.DimenUtils


class DaySwitch : View, View.OnClickListener {

    private var accentPaint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var textPaint: Paint = Paint().apply {
        isAntiAlias = true
        textSize = DimenUtils.dpToPx(18f).toFloat()
        textAlign = Paint.Align.CENTER
    }

    private var clippedTextPaint: Paint = Paint().apply {
        isAntiAlias = true
        textSize = DimenUtils.dpToPx(18f).toFloat()
        textAlign = Paint.Align.CENTER
    }

    private var checked: Float = 0f

    var isChecked: Boolean = false
        set(isChecked) {
            if (isChecked != this.isChecked) {
                field = isChecked
                textPaint.color = if (isChecked) textColorPrimaryInverse else textColorPrimary

                run {
                    if (isChecked)
                        ValueAnimator.ofFloat(0f, 1f)
                    else ValueAnimator.ofFloat(1f, 0f)
                }.apply {
                    interpolator = if (isChecked) DecelerateInterpolator() else AnticipateOvershootInterpolator()
                    addUpdateListener { valueAnimator ->
                        checked = valueAnimator.animatedValue as? Float ?: 0f
                        invalidate()
                    }
                    start()
                }
            }
        }

    private var textColorPrimary: Int = 0
    private var textColorPrimaryInverse: Int = 0
    private var text: String? = null

    var onCheckedChangeListener: OnCheckedChangeListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setOnClickListener(this)
    }

    /**
     * Set the text (a single letter, usually) to display
     * in the switch.
     */
    fun setText(text: String) {
        this.text = text
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        text?.let { str ->
            // Calculate text size to not extend past circle radius ( - 4dp for padding)
            val textWidth = textPaint.measureText(str)
            val circleWidth = DimenUtils.dpToPx(32f)
            if (textWidth > circleWidth) {
                textPaint.textSize *= (circleWidth.toFloat() / textWidth)
                clippedTextPaint.textSize = textPaint.textSize
            }

            canvas.drawText(str, (width / 2).toFloat(), height / 2 - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
        }

        val circlePath = Path()
        circlePath.addCircle((width / 2).toFloat(), (height / 2).toFloat(), checked * DimenUtils.dpToPx(18f), Path.Direction.CW)
        circlePath.close()

        canvas.drawPath(circlePath, accentPaint)

        text?.let { str ->
            canvas.drawText(str, (width / 2).toFloat(), height / 2 - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
            canvas.clipPath(circlePath)
            canvas.drawText(str, (width / 2).toFloat(), height / 2 - (textPaint.descent() + textPaint.ascent()) / 2, clippedTextPaint)
        }
    }

    override fun onClick(view: View) {
        isChecked = !this.isChecked
        onCheckedChangeListener?.onCheckedChanged(this, this.isChecked)
    }

    /**
     * A listener to be invoked whenever the checked state
     * of the view is modified.
     */
    interface OnCheckedChangeListener {

        /**
         * Called when the state is changed.
         *
         * @param daySwitch The switch view that was changed.
         * @param isChecked Whether the switch is checked (boolean).
         */
        fun onCheckedChanged(daySwitch: DaySwitch, isChecked: Boolean)
    }
}
