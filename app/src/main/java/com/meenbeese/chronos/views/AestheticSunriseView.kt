package com.meenbeese.chronos.views

import android.content.Context
import android.util.AttributeSet

import com.afollestad.aesthetic.Aesthetic
import com.meenbeese.chronos.interfaces.Subscribable

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy


/**
 * A SunriseView extension class that implements
 * Aesthetic theming.
 */
class AestheticSunriseView : SunriseSunsetView, Subscribable {

    private var colorAccentSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null
    private val disposables = CompositeDisposable()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        isClickable = false
        isFocusable = false
    }

    override fun subscribe() {
        textColorPrimarySubscription = Aesthetic.get()
            .textColorPrimary()
            .subscribeBy(
                onNext = { integer ->
                    sunsetColor = (200 shl 24) or (integer and 0x00FFFFFF)
                    futureColor = (20 shl 24) or (integer and 0x00FFFFFF)
                    postInvalidate()
                },
                onError = { it.printStackTrace() }
            ).also { disposables.add(it) }

        colorAccentSubscription = Aesthetic.get()
            .colorAccent()
            .subscribeBy(
                onNext = { integer ->
                    sunriseColor = (200 shl 24) or (integer and 0x00FFFFFF)
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
}
