package com.meenbeese.chronos.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet

import androidx.core.widget.CompoundButtonCompat

import com.afollestad.aesthetic.Aesthetic
import com.google.android.material.checkbox.MaterialCheckBox
import com.meenbeese.chronos.interfaces.Subscribable

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy


/**
 * A MaterialCheckBox extension class that
 * implements Aesthetic theming.
 */
class AestheticCheckBoxView : MaterialCheckBox, Subscribable {

    private var colorAccentSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null
    private val disposables = CompositeDisposable()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun subscribe() {
        colorAccentSubscription = Aesthetic.get().colorAccent()
            .subscribeBy(
                onNext = { integer ->
                    val colorStateList = ColorStateList(
                        arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                        intArrayOf(Color.argb(255, 128, 128, 128), integer)
                    )
                    CompoundButtonCompat.setButtonTintList(this, colorStateList)
                },
                onError = { it.printStackTrace() }
            ).also { disposables.add(it) }

        textColorPrimarySubscription = Aesthetic.get().textColorPrimary()
            .subscribeBy(
                onNext = { integer -> setTextColor(integer) },
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
