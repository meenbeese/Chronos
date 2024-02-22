package com.meenbeese.chronos.views

import android.content.Context
import android.util.AttributeSet

import com.afollestad.aesthetic.Aesthetic
import com.meenbeese.chronos.interfaces.Subscribable

import me.jfenn.slideactionview.SlideActionView

import io.reactivex.disposables.Disposable


/**
 * A SlideActionView extension class that implements
 * Aesthetic theming.
 */
class AestheticSlideActionView : SlideActionView, Subscribable {

    private var textColorPrimarySubscription: Disposable? = null
    private var textColorPrimaryInverseSubscription: Disposable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun subscribe() {
        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe { integer ->
                    touchHandleColor = integer
                    outlineColor = integer
                    iconColor = integer
                    postInvalidate()
                }

        textColorPrimaryInverseSubscription = Aesthetic.get()
                .textColorPrimaryInverse()
                .subscribe { integer -> setBackgroundColor((100 shl 24) or (integer and 0x00ffffff)) }
    }

    override fun unsubscribe() {
        textColorPrimarySubscription?.dispose()
        textColorPrimaryInverseSubscription?.dispose()
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
