package com.meenbeese.chronos.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View


/**
 * Display a progress line, with a given foreground/background
 * color set.
 */
class ProgressLineView : View {

    private var backgroundPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.LTGRAY
    }

    private var linePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.DKGRAY
    }

    private var progress: Float = 0f
    private var drawnProgress: Float = 0f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun update(progress: Float) {
        this.progress = progress
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (drawnProgress != progress)
            drawnProgress = (drawnProgress * 4 + progress) / 5

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        canvas.drawRect(0f, 0f, width * drawnProgress, height.toFloat(), linePaint)

        if ((drawnProgress - progress) * width != 0f)
            postInvalidate()
    }
}
