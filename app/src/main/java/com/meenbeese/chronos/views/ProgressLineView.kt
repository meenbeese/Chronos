package com.meenbeese.chronos.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

import com.afollestad.aesthetic.Aesthetic
import com.meenbeese.chronos.interfaces.Subscribable

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy


/**
 * Display a progress line, with a given foreground/background
 * color set.
 */
class ProgressLineView : View, Subscribable {

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

    private var colorAccentSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null
    private val disposables = CompositeDisposable()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun subscribe() {
        colorAccentSubscription = Aesthetic.get()
            .colorAccent()
            .subscribeBy(
                onNext = { integer ->
                    linePaint.color = integer
                    linePaint.alpha = 100
                    postInvalidate()
                },
                onError = { it.printStackTrace() }
            ).also { disposables.add(it) }

        textColorPrimarySubscription = Aesthetic.get()
            .textColorPrimary()
            .subscribeBy(
                onNext = { integer ->
                backgroundPaint.color = integer
                backgroundPaint.alpha = 30
                postInvalidate()
                },
                onError = { it.printStackTrace() }
            ).also { disposables.add(it) }
    }

    override fun unsubscribe() {
        disposables.clear()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        subscribe()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unsubscribe()
    }

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
