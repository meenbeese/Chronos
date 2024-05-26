package com.meenbeese.chronos.utils;

import androidx.annotation.Nullable;

/**
 * The FloatUtils class animates a float, to a granularity of
 * 0.1f. That is, if the difference between the target and current
 * value is less than 0.1, it will be ignored and the animation will
 * be regarded as complete.
 */
public class FloatUtils {

    public final static long DEFAULT_ANIMATION_DURATION = 400;

    private Float targetValue;
    private Float drawnValue;

    @Nullable
    private Float defaultValue;

    private long start;

    public FloatUtils(Float value) {
        targetValue = drawnValue = value;
    }

    /**
     * Set the current value to be drawn.
     *
     * @param value         The current value.
     */
    public void set(Float value) {
        drawnValue = value;
    }

    /**
     * Set the default value to return to.
     *
     * @param defaultValue  The default value.
     */
    public void setDefault(Float defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Set the current (and target) value.
     *
     * @param value         The current value.
     */
    public void setCurrent(Float value) {
        drawnValue = targetValue = value;
    }

    /**
     * Get the current value to be drawn.
     *
     * @return              The current value.
     */
    public Float val() {
        return drawnValue;
    }

    /**
     * Get the next value about to be drawn, without setting
     * the current value to it.
     *
     * @return              The next value.
     */
    public Float nextVal() {
        return nextVal(DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Get the next value about to be drawn, without setting
     * the current value to it.
     *
     * @param duration      The duration, in milliseconds, that
     *                      the animation should take.
     * @return              The next value.
     */
    public Float nextVal(long duration) {
        return nextVal(start, duration);
    }

    /**
     * Get the next value about to be drawn, without setting
     * the current value to it.
     *
     * @param start         The time at which the animation started,
     *                      in milliseconds.
     * @param duration      The duration, in milliseconds, that
     *                      the animation should take.
     * @return              The next value.
     */
    public Float nextVal(long start, long duration) {
        float difference = (getTarget() - val()) * (float) Math.sqrt((double) (System.currentTimeMillis() - start) / (duration));
        if (Math.abs(getTarget() - val()) > .1f && System.currentTimeMillis() - start < duration)
            return val() + (getTarget() < val() ? Math.min(difference, -.1f) : Math.max(difference, .1f));
        else return getTarget();
    }

    /**
     * Get the target value that is currently being animated to.
     *
     * @return              The target value.
     */
    public Float getTarget() {
        return targetValue;
    }

    /**
     * Get the default value that the animation should return to.
     *
     * @return              The default value.
     */
    public Float getDefault() {
        return defaultValue != null ? defaultValue : targetValue;
    }

    /**
     * Determine if the target value has been drawn (implying that
     * the animation is complete).
     *
     * @return              True if the target value has supposedly
     *                      been drawn.
     */
    public boolean isTarget() {
        return drawnValue.equals(targetValue);
    }

    /**
     * Determine if the default value has been drawn.
     *
     * @return              True if the default value has supposedly
     *                      been drawn.
     */
    public boolean isDefault() {
        return drawnValue.equals(defaultValue);
    }

    /**
     * Determine if the default value has been set AND is the current
     * target.
     *
     * @return              True if the default value is the current
     *                      target.
     */
    public boolean isTargetDefault() {
        return targetValue.equals(defaultValue);
    }

    /**
     * Animate to the default value.
     */
    public void toDefault() {
        if (defaultValue != null)
            to(defaultValue);
    }

    /**
     * Set the value to be animated towards.
     *
     * @param value         The target value.
     */
    public void to(Float value) {
        targetValue = value;
        start = System.currentTimeMillis();
    }

    /**
     * Update the current value.
     *
     * @param animate       Whether to animate the change.
     * @param duration      The duration, in milliseconds, to animate
     *                      across.
     */
    public void next(boolean animate, long duration) {
        drawnValue = animate ? nextVal(duration) : targetValue;
    }

    /**
     * Update the current value.
     *
     * @param animate       Whether to animate the change.
     */
    public void next(boolean animate) {
        next(animate, DEFAULT_ANIMATION_DURATION);
    }
}
