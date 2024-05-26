package com.meenbeese.chronos.utils;

import androidx.annotation.Nullable;


/**
 * The AnimFloat class animates a float, to a granularity of
 * 0.1f. That is, if the difference between the target and current
 * value is less than 0.1, it will be ignored and the animation will
 * be regarded as complete.
 */
public class AnimFloat {

    public final static long DEFAULT_ANIM_DURATION = 400;

    private Float targetVal;
    private Float drawnVal;

    @Nullable
    private Float defaultVal;

    private long startTime;

    public AnimFloat(Float value) {
        targetVal = drawnVal = value;
    }

    /**
     * Set the current value to be drawn.
     *
     * @param value         The current value.
     */
    public void setDrawnValue(Float value) {
        drawnVal = value;
    }

    /**
     * Set the default value to return to.
     *
     * @param defaultVal  The default value.
     */
    public void setDefaultValue(Float defaultVal) {
        this.drawnVal = defaultVal;
    }

    /**
     * Set the current (and target) value.
     *
     * @param value         The current value.
     */
    public void setCurrentValue(Float value) {
        drawnVal = targetVal = value;
    }

    /**
     * Get the current value to be drawn.
     *
     * @return              The current value.
     */
    public Float getDrawnValue() {
        return drawnVal;
    }

    /**
     * Get the next value about to be drawn, without setting
     * the current value to it.
     *
     * @return              The next value.
     */
    public Float getNextValue() {
        return getNextValue(DEFAULT_ANIM_DURATION);
    }

    /**
     * Get the next value about to be drawn, without setting
     * the current value to it.
     *
     * @param duration      The duration, in milliseconds, that
     *                      the animation should take.
     * @return              The next value.
     */
    public Float getNextValue(long duration) {
        return getNextValue(startTime, duration);
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
    public Float getNextValue(long start, long duration) {
        float difference = (getTargetValue() - getDrawnValue()) * (float) Math.sqrt((double) (System.currentTimeMillis() - start) / (duration));
        if (Math.abs(getTargetValue() - getDrawnValue()) > .1f && System.currentTimeMillis() - start < duration)
            return getDrawnValue() + (getTargetValue() < getDrawnValue() ? Math.min(difference, -.1f) : Math.max(difference, .1f));
        else return getTargetValue();
    }

    /**
     * Get the target value that is currently being animated to.
     *
     * @return              The target value.
     */
    public Float getTargetValue() {
        return targetVal;
    }

    /**
     * Get the default value that the animation should return to.
     *
     * @return              The default value.
     */
    public Float getDefaultValue() {
        return defaultVal != null ? defaultVal : targetVal;
    }

    /**
     * Determine if the target value has been drawn (implying that
     * the animation is complete).
     *
     * @return              True if the target value has supposedly
     *                      been drawn.
     */
    public boolean isTargetValue() {
        return drawnVal.equals(targetVal);
    }

    /**
     * Determine if the default value has been drawn.
     *
     * @return              True if the default value has supposedly
     *                      been drawn.
     */
    public boolean isDefault() {
        return drawnVal.equals(defaultVal);
    }

    /**
     * Determine if the default value has been set AND is the current
     * target.
     *
     * @return              True if the default value is the current
     *                      target.
     */
    public boolean isTargetDefault() {
        return targetVal.equals(defaultVal);
    }

    /**
     * Animate to the default value.
     */
    public void toDefault() {
        if (defaultVal != null)
            setTargetValue(defaultVal);
    }

    /**
     * Set the value to be animated towards.
     *
     * @param value         The target value.
     */
    public void setTargetValue(Float value) {
        targetVal = value;
        startTime = System.currentTimeMillis();
    }

    /**
     * Update the current value.
     *
     * @param animate       Whether to animate the change.
     * @param duration      The duration, in milliseconds, to animate
     *                      across.
     */
    public void updateValue(boolean animate, long duration) {
        drawnVal = animate ? getNextValue(duration) : targetVal;
    }

    /**
     * Update the current value.
     *
     * @param animate       Whether to animate the change.
     */
    public void updateValue(boolean animate) {
        updateValue(animate, DEFAULT_ANIM_DURATION);
    }
}
