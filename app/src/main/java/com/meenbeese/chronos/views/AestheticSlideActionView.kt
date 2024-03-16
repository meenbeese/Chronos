package com.meenbeese.chronos.views

import android.content.Context
import android.util.AttributeSet

import com.afollestad.aesthetic.Aesthetic
import com.meenbeese.chronos.interfaces.Subscribable

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy


/**
 * A SlideActionView extension class that implements
 * Aesthetic theming.
 */
class AestheticSlideActionView : SlideActionView, Subscribable {

    private var textColorPrimarySubscription: Disposable? = null
    private var textColorPrimaryInverseSubscription: Disposable? = null
    private val disposables = CompositeDisposable()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun subscribe() {
        textColorPrimarySubscription = Aesthetic.get()
            .textColorPrimary()
            .subscribeBy(
                onNext = { integer ->
                    touchHandleColor = integer
                    outlineColor = integer
                    iconColor = integer
                    postInvalidate()
                },
                onError = { it.printStackTrace() }
            ).also { disposables.add(it) }

        textColorPrimaryInverseSubscription = Aesthetic.get()
            .textColorPrimaryInverse()
            .subscribeBy(
                onNext = { integer ->
                    setBackgroundColor((100 shl 24) or (integer and 0x00ffffff))
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
