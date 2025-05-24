package com.meenbeese.chronos.utils

import android.content.Context
import android.content.res.Resources

object DimenUtils {
    /**
     * Returns the height of the device's status bar in px.
     *
     * @param context       An active context instance.
     * @return              The height of the status bar, in pixels.
     */
    fun Context.getStatusBarHeight(): Int {
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) resources.getDimensionPixelSize(resId) else 0
    }

    /**
     * Returns the height of the device's navigation bar in px.
     *
     * @param context       An active context instance.
     * @return              The height of the navigation bar, in pixels.
     */
    fun Context.getNavigationBarHeight(): Int {
        val resId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resId > 0) resources.getDimensionPixelSize(resId) else 0
    }

    /**
     * Converts dp units to pixels.
     *
     * @param dp            A distance measurement, in dp.
     * @return              The value of the provided dp units, in pixels.
     */
    fun dpToPx(dp: Float): Int {
        return (Resources.getSystem().displayMetrics.density * dp).toInt()
    }

    /**
     * Converts pixels to dp.
     *
     * @param pixels        A distance measurement, in pixels.
     * @return              The value of the provided pixel units, in dp.
     */
    fun pxToDp(pixels: Int): Float {
        return pixels / Resources.getSystem().displayMetrics.density
    }

    /**
     * Converts sp to pixels.
     *
     * @param sp            A distance measurement, in sp.
     * @return              The value of the provided sp units, in pixels.
     */
    fun spToPx(sp: Float): Int {
        return (Resources.getSystem().displayMetrics.scaledDensity * sp).toInt()
    }

    /**
     * Converts pixels to sp.
     *
     * @param pixels        A distance measurement, in pixels.
     * @return              The value of the provided pixel units, in sp.
     */
    fun pxToSp(pixels: Int): Float {
        return pixels / Resources.getSystem().displayMetrics.scaledDensity
    }
}