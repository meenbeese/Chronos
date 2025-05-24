package com.meenbeese.chronos.utils

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * The AnimFloat class animates a float, to a granularity of
 * 0.1f. That is, if the difference between the target and current
 * value is less than 0.1, it will be ignored and the animation will
 * be regarded as complete.
 */
class AnimFloat(var drawnValue: Float) {
    private var targetVal: Float
    private var startTime: Long = 0

    init {
        targetVal = drawnValue
    }

    /**
     * Set the current (and target) value.
     *
     * @param value         The current value.
     */
    fun setCurrentValue(value: Float) {
        targetVal = value
        drawnValue = targetVal
    }

    /**
     * Get the next value about to be drawn, without setting
     * the current value to it.
     *
     * @param duration      The duration, in milliseconds, that
     * the animation should take.
     * @return              The next value.
     */
    private fun getNextValue(duration: Long): Float {
        return getNextValue(startTime, duration)
    }

    /**
     * Get the next value about to be drawn, without setting
     * the current value to it.
     *
     * @param start         The time at which the animation started, in milliseconds.
     * @param duration      The duration, in milliseconds, that the animation should take.
     *
     * @return              The next value.
     */
    private fun getNextValue(start: Long, duration: Long): Float {
        val elapsedTime = (System.currentTimeMillis() - start).toDouble()
        val difference = ((targetValue - drawnValue) * sqrt(elapsedTime / duration)).toFloat()
        val minMaxDifference = if (targetValue < drawnValue) min(difference.toDouble(), -.1) else max(difference.toDouble(), .1)
        val isWithinDuration = elapsedTime < duration
        val isSignificantDifference = abs(targetValue - drawnValue) > .1f

        return when {
            isSignificantDifference && isWithinDuration -> (drawnValue + minMaxDifference).toFloat()
            else -> targetValue
        }
    }

    var targetValue: Float
        /**
         * Get the target value that is currently being animated to.
         *
         * @return              The target value.
         */
        get() = targetVal
        /**
         * Set the value to be animated towards.
         *
         * @param value         The target value.
         */
        set(value) {
            targetVal = value
            startTime = System.currentTimeMillis()
        }

    /**
     * Determine if the target value has been drawn (implying that
     * the animation is complete).
     *
     * @return              True if the target value has supposedly
     * been drawn.
     */
    fun isTargetValue(): Boolean {
        return drawnValue == targetVal
    }

    /**
     * Update the current value.
     *
     * @param animate       Whether to animate the change.
     * @param duration      The duration, in milliseconds, to animate across.
     *
     * @param animate       Whether to animate the change.
     */
    @JvmOverloads
    fun updateValue(animate: Boolean, duration: Long = DEFAULT_ANIM_DURATION) {
        drawnValue = if (animate) getNextValue(duration) else targetVal
    }

    companion object {
        const val DEFAULT_ANIM_DURATION: Long = 400
    }
}
