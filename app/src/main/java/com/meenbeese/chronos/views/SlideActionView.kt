package com.meenbeese.chronos.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

import androidx.annotation.ColorInt

import com.meenbeese.chronos.interfaces.SlideActionListener
import com.meenbeese.chronos.utils.DimenUtils.dpToPx
import com.meenbeese.chronos.utils.ImageUtils.toBitmap
import com.meenbeese.chronos.utils.AnimFloat

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


open class SlideActionView : View, OnTouchListener {
    private var position = -1f
    private var handleRadius = 0
    private var expandedHandleRadius = 0
    private var selectionRadius = 0
    private var rippleRadius = 0
    private var normalPaint: Paint? = null
    private var outlinePaint: Paint? = null
    private var bitmapPaint: Paint? = null
    private var leftImage: Bitmap? = null
    private var rightImage: Bitmap? = null
    private var listener: SlideActionListener? = null
    private lateinit var selected: AnimFloat
    private lateinit var ripples: MutableMap<Float, AnimFloat>

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
        handleRadius = dpToPx(12f)
        expandedHandleRadius = dpToPx(32f)
        selectionRadius = dpToPx(42f)
        rippleRadius = dpToPx(140f)
        selected = AnimFloat(0f)
        ripples = HashMap()

        normalPaint = Paint()
        normalPaint?.style = Paint.Style.FILL
        normalPaint?.color = Color.GRAY
        normalPaint?.isAntiAlias = true
        normalPaint?.isDither = true

        outlinePaint = Paint()
        outlinePaint?.style = Paint.Style.STROKE
        outlinePaint?.color = Color.GRAY
        outlinePaint?.isAntiAlias = true
        outlinePaint?.isDither = true

        bitmapPaint = Paint()
        bitmapPaint?.style = Paint.Style.FILL
        bitmapPaint?.color = Color.GRAY
        bitmapPaint?.isAntiAlias = true
        bitmapPaint?.isDither = true
        bitmapPaint?.isFilterBitmap = true

        setOnTouchListener(this)
        isFocusable = true
        isClickable = true
    }

    /**
     * Specify an interface to pass events to when an action
     * is selected.
     *
     * @param listener          An interface to pass events to.
     */
    fun setListener(listener: SlideActionListener?) {
        this.listener = listener
    }

    /**
     * Specifies the icon to display on the left side of the view,
     * as a Drawable. If it is just as easier to pass a Bitmap, you
     * should avoid using this method; all it does is convert the
     * drawable to a bitmap, then call the same method again.
     *
     * @param drawable          The Drawable to use as an icon.
     */
    fun setLeftIcon(drawable: Drawable) {
        setLeftIcon(drawable.toBitmap())
    }

    /**
     * Specifies the icon to display on the left side of the view.
     *
     * @param bitmap            The Bitmap to use as an icon.
     */
    private fun setLeftIcon(bitmap: Bitmap?) {
        leftImage = bitmap
        postInvalidate()
    }

    /**
     * Specifies the icon to display on the right side of the view,
     * as a Drawable. If it is just as easier to pass a Bitmap, you
     * should avoid using this method; all it does is convert the
     * drawable to a bitmap, then call the same method again.
     *
     * @param drawable          The Drawable to use as an icon.
     */
    fun setRightIcon(drawable: Drawable) {
        setRightIcon(drawable.toBitmap())
    }

    /**
     * Specifies the icon to display on the right side of the view.
     *
     * @param bitmap            The Bitmap to use as an icon.
     */
    private fun setRightIcon(bitmap: Bitmap?) {
        rightImage = bitmap
        postInvalidate()
    }

    @get:ColorInt
    var touchHandleColor: Int
        /**
         * @return The color of the touch handle in the center of the view.
         */
        get() = normalPaint!!.color
        /**
         * Specify the color of the touch handle in the center of
         * the view. The alpha of this color is modified to be somewhere
         * between 0 and 150.
         *
         * @param handleColor       The color of the touch handle.
         */
        set(handleColor) {
            normalPaint!!.color = handleColor
        }

    @get:ColorInt
    var outlineColor: Int
        /**
         * @return The color of the random outlines drawn all over the place.
         */
        get() = outlinePaint!!.color
        /**
         * Specify the color of the random outlines drawn all over the place.
         *
         * @param outlineColor      The color of the random outlines.
         */
        set(outlineColor) {
            outlinePaint?.color = outlineColor
        }

    @get:ColorInt
    var iconColor: Int
        /**
         * @return The color applied to the left/right icons as a filter.
         */
        get() = bitmapPaint!!.color
        /**
         * Specify the color applied to the left/right icons as a filter.
         *
         * @param iconColor         The color that the left/right icons are filtered by.
         */
        set(iconColor) {
            bitmapPaint?.color = iconColor
            bitmapPaint?.setColorFilter(PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN))
        }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        selected.updateValue(true)
        position = if (position < 0) width.toFloat() / 2 else position
        drawCircle(canvas)
        drawImages(canvas)
        drawOutline(canvas)
        drawRipples(canvas)
        if (!selected.isTargetValue() || ripples.isNotEmpty()) postInvalidate()
    }

    private fun drawCircle(canvas: Canvas) {
        normalPaint?.alpha = 150 - (selected.drawnValue * 100).toInt()
        val radius = (handleRadius * (1 - selected.drawnValue) + expandedHandleRadius * selected.drawnValue).toInt()
        val drawnX = position * selected.drawnValue + width.toFloat() / 2 * (1 - selected.drawnValue)
        canvas.drawCircle(drawnX, height.toFloat() / 2, radius.toFloat(), normalPaint!!)
    }

    private fun drawImages(canvas: Canvas) {
        if (leftImage != null && rightImage != null) {
            val drawnX = position * selected.drawnValue + width.toFloat() / 2 * (1 - selected.drawnValue)
            bitmapPaint?.alpha = (255 * min(
                1.0,
                max(0.0, ((width - drawnX - selectionRadius) / width).toDouble())
            )).toInt()
            canvas.drawBitmap(
                leftImage!!,
                selectionRadius - leftImage!!.width.toFloat() / 2,
                (height - leftImage!!.height).toFloat() / 2,
                bitmapPaint
            )
            bitmapPaint?.alpha = (255 * min(
                1.0,
                max(0.0, ((drawnX - selectionRadius) / width).toDouble())
            )).toInt()
            canvas.drawBitmap(
                rightImage!!,
                width - selectionRadius - leftImage!!.width.toFloat() / 2,
                (height - leftImage!!.height).toFloat() / 2,
                bitmapPaint
            )
        }
    }

    private fun drawOutline(canvas: Canvas) {
        val drawnX = position * selected.drawnValue + width.toFloat() / 2 * (1 - selected.drawnValue)
        if (abs((width.toFloat() / 2 - drawnX).toDouble()) > selectionRadius.toFloat() / 2) {
            var progress = if (drawnX * 2 < width) min(
                1.0,
                max(
                    0.0,
                    ((width - (drawnX + selectionRadius) * 2) / width).toDouble()
                )
            ).toFloat() else min(
                1.0,
                max(
                    0.0,
                    (((drawnX - selectionRadius) * 2 - width) / width).toDouble()
                )
            ).toFloat()
            progress = progress.pow(0.2f)
            outlinePaint?.alpha = (255 * progress).toInt()
            val circleX = (if (drawnX * 2 < width) selectionRadius else width - selectionRadius).toFloat()
            canvas.drawCircle(
                circleX,
                height.toFloat() / 2,
                selectionRadius.toFloat() / 2 + rippleRadius * (1 - progress),
                outlinePaint!!
            )
        }
    }

    private fun drawRipples(canvas: Canvas) {
        for (key in ripples.keys) {
            val scale = ripples[key]!!
            scale.updateValue(true, 1600)
            normalPaint?.alpha = (150 * (scale.targetValue - scale.drawnValue) / scale.targetValue).toInt()
            canvas.drawCircle(key, height.toFloat() / 2, scale.drawnValue, normalPaint!!)
            if (scale.isTargetValue()) ripples.remove(key)
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val eventX = event.x
        val halfWidth = width.toFloat() / 2
        val isActionDown = event.action == MotionEvent.ACTION_DOWN
        val isActionUp = event.action == MotionEvent.ACTION_UP
        if (isActionDown && abs((eventX - halfWidth).toDouble()) < selectionRadius) {
            selected.setCurrentValue(1f)
        } else if (isActionUp && selected.targetValue > 0) {
            handleActionUp(eventX)
            return true
        }
        if (selected.targetValue > 0) {
            position = eventX
            postInvalidate()
        }
        return false
    }

    private fun handleActionUp(eventX: Float) {
        selected.setCurrentValue(0f)
        val rippleStart = (if (eventX > width - selectionRadius * 2) width - selectionRadius else selectionRadius).toFloat()
        createRipple(rippleStart)
        postInvalidate()
    }

    private fun createRipple(rippleStart: Float) {
        val ripple = AnimFloat(selectionRadius.toFloat())
        ripple.targetValue = rippleRadius.toFloat()
        ripples[rippleStart] = ripple
        if (listener != null) {
            if (rippleStart == selectionRadius.toFloat()) {
                listener?.onSlideLeft()
            } else {
                listener?.onSlideRight()
            }
        }
    }
}
