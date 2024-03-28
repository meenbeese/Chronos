package com.meenbeese.chronos.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener

import com.afollestad.aesthetic.Aesthetic.Companion.get
import com.meenbeese.chronos.interfaces.Subscribable
import com.meenbeese.chronos.utils.DimenUtils

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy


class PageIndicatorView : View, OnPageChangeListener, Subscribable {
    var actualPosition = 0
        private set
    var positionOffset = 0f
        private set
    var totalPages = 0
        private set
    private var engine: IndicatorEngine? = null
    private var textColorPrimary = 0
    private var textColorSecondary = 0
    private var textColorPrimarySubscription: Disposable? = null
    private var textColorSecondarySubscription: Disposable? = null
    private val disposables = CompositeDisposable()

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
        engine = IndicatorEngine()
        engine?.onInitEngine(this)
        totalPages = 2
    }

    override fun subscribe() {
        textColorPrimarySubscription = get()
            .textColorPrimary()
            .subscribeBy(
                onNext = { integer: Int ->
                    textColorPrimary = integer
                    engine?.updateTextColors(this@PageIndicatorView)
                    invalidate()
                },
                onError = { it.printStackTrace() }
            ).also { disposables.add(it) }

        textColorSecondarySubscription = get()
            .textColorSecondary()
            .subscribeBy(
                onNext = { integer: Int ->
                    textColorSecondary = integer
                    engine?.updateTextColors(this@PageIndicatorView)
                    invalidate()
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        engine?.onDrawIndicator(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        engine?.let { setMeasuredDimension(it.measuredWidth, it.measuredHeight) }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        actualPosition = position
        this.positionOffset = positionOffset
        invalidate()
    }

    override fun onPageSelected(position: Int) {}
    override fun onPageScrollStateChanged(state: Int) {}

    /**
     * You must call this AFTER setting the Adapter for the ViewPager, or it won't display the right amount of points.
     */
    fun setViewPager(viewPager: ViewPager) {
        viewPager.addOnPageChangeListener(this)
        totalPages = viewPager.adapter!!.count
        invalidate()
    }

    private class IndicatorEngine {
        private lateinit var indicator: PageIndicatorView
        private lateinit var selectedPaint: Paint
        private lateinit var unselectedPaint: Paint
        val measuredHeight: Int
            get() = DimenUtils.dpToPx(8f)
        val measuredWidth: Int
            get() = DimenUtils.dpToPx((8 * (indicator.totalPages * 2 - 1)).toFloat())

        fun onInitEngine(indicator: PageIndicatorView) {
            this.indicator = indicator
            selectedPaint = Paint()
            unselectedPaint = Paint()
            selectedPaint.color = indicator.textColorPrimary
            unselectedPaint.color = indicator.textColorSecondary
            selectedPaint.flags = Paint.ANTI_ALIAS_FLAG
            unselectedPaint.flags = Paint.ANTI_ALIAS_FLAG
        }

        fun updateTextColors(indicator: PageIndicatorView) {
            selectedPaint.color = indicator.textColorPrimary
            unselectedPaint.color = indicator.textColorSecondary
        }

        fun onDrawIndicator(canvas: Canvas) {
            val height = indicator.height
            for (i in 0 until indicator.totalPages) {
                val x = DimenUtils.dpToPx(4f) + DimenUtils.dpToPx((16 * i).toFloat())
                canvas.drawCircle(
                    x.toFloat(),
                    height / 2f,
                    DimenUtils.dpToPx(4f).toFloat(),
                    unselectedPaint
                )
            }
            var firstX: Int = DimenUtils.dpToPx((4 + indicator.actualPosition * 16).toFloat())
            if (indicator.positionOffset > .5f) {
                firstX += DimenUtils.dpToPx(16 * (indicator.positionOffset - .5f) * 2)
            }
            var secondX: Int = DimenUtils.dpToPx((4 + indicator.actualPosition * 16).toFloat())
            secondX += if (indicator.positionOffset < .5f) {
                DimenUtils.dpToPx(16 * indicator.positionOffset * 2)
            } else {
                DimenUtils.dpToPx(16f)
            }
            canvas.drawCircle(
                firstX.toFloat(),
                DimenUtils.dpToPx(4f).toFloat(),
                DimenUtils.dpToPx(4f).toFloat(),
                selectedPaint
            )
            canvas.drawCircle(
                secondX.toFloat(),
                DimenUtils.dpToPx(4f).toFloat(),
                DimenUtils.dpToPx(4f).toFloat(),
                selectedPaint
            )
            canvas.drawRect(
                firstX.toFloat(),
                0f,
                secondX.toFloat(),
                DimenUtils.dpToPx(8f).toFloat(),
                selectedPaint
            )
        }
    }
}