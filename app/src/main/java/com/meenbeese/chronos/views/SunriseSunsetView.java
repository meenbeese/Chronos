package com.meenbeese.chronos.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.meenbeese.chronos.utils.AnimFloat;

import java.util.Calendar;


public class SunriseSunsetView extends View implements View.OnTouchListener {

    private static final float DAY_START = 0f;
    private static final float DAY_END = 0.99998842592f;
    private static final float DAY_HOUR = 0.04167f;

    private static final long DAY_LENGTH = 86400000L;

    private Paint sunrisePaint;
    private Paint sunsetPaint;
    private Paint linePaint;

    private AnimFloat dayStart;
    private AnimFloat dayEnd;

    private Float moveBeginStart;
    private Float moveBeginEnd;
    private float min = DAY_HOUR, max = DAY_END;

    private SunriseListener listener;

    public SunriseSunsetView(Context context) {
        super(context);
        init();
    }

    public SunriseSunsetView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SunriseSunsetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dayStart = new AnimFloat(0.25f);
        dayEnd = new AnimFloat(0.75f);

        sunrisePaint = new Paint();
        sunrisePaint.setAntiAlias(true);
        sunrisePaint.setStyle(Paint.Style.FILL);
        sunrisePaint.setColor(Color.BLACK);

        sunsetPaint = new Paint();
        sunsetPaint.setAntiAlias(true);
        sunsetPaint.setStyle(Paint.Style.FILL);
        sunsetPaint.setColor(Color.BLACK);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.BLACK);
        linePaint.setAlpha(20);

        setOnTouchListener(this);
        setClickable(true);
        setFocusable(true);
    }

    /**
     * Sets the color for the segment of the day where the
     * sun is above the horizon.
     *
     * @param color                 The sunrise color.
     */
    public void setSunriseColor(@ColorInt int color) {
        sunrisePaint.setColor(color);
        postInvalidate();
    }

    /**
     * @return The color of the segment of the day where the
     *         sun is above the horizon.
     */
    @ColorInt
    public int getSunriseColor() {
        return sunrisePaint.getColor();
    }

    /**
     * Sets the color for the segment of the day where the
     * sun is below the horizon.
     *
     * @param color                 The sunset color.
     */
    public void setSunsetColor(@ColorInt int color) {
        sunsetPaint.setColor(color);
        postInvalidate();
    }

    /**
     * @return The color of the segment of the day where the
     *         sun is below the horizon.
     */
    @ColorInt
    public int getSunsetColor() {
        return sunsetPaint.getColor();
    }

    /**
     * Sets the color for the segment of the day that has
     * not passed yet; some may refer to it as the future,
     * but what truly is the future but a moment in time
     * which has yet to occur? Since nobody truly knows the
     * exact outcome of a future event, the future cannot
     * possibly exist until it actually happens. With that
     * said, we refer to an event which we believe might
     * happen as the future, but there is no fail-safe method
     * of proving that said event will actually occur, short
     * of it occurring. Can one really say it is possible to
     * determine what is essentially an abstraction of a
     * future event with no uncertainty that it might not
     * occur?
     *
     * @param color                 The future color.
     */
    public void setFutureColor(@ColorInt int color) {
        linePaint.setColor(color);
        postInvalidate();
    }

    /**
     * @return The color of the segment of the day which
     *         has yet to occur.
     */
    @ColorInt
    public int getFutureColor() {
        return linePaint.getColor();
    }

    /**
     * Set the sunrise time, in milliseconds. Values can range
     * beyond the period of a day; they are modulated by a 24 hour
     * period. Change in values will not be animated.
     *
     * @param dayStartMillis            The sunrise time, in milliseconds.
     */
    public void setSunrise(long dayStartMillis) {
        setSunrise(dayStartMillis, false);
    }

    /**
     * Set the minimum and maximum distance allowed between sunrise
     * and sunset.
     *
     * @param minMinutes                The minimum distance, in minutes.
     * @param maxMinutes                The maximum distance, in minutes.
     */
    public void setMinMax(int minMinutes, int maxMinutes) {
        min = (float) minMinutes / 1440;
        max = (float) maxMinutes / 1440;
    }

    /**
     * Set the sunrise time, in milliseconds. Values can range
     * beyond the period of a day; they are modulated by a 24 hour
     * period.
     *
     * @param dayStartMillis            The sunrise time, in milliseconds.
     * @param animate                   Whether to animate the change in
     *                                  values.
     */
    public void setSunrise(long dayStartMillis, boolean animate) {
        dayStartMillis %= DAY_LENGTH;
        if (animate)
            dayStart.setTargetValue((float) dayStartMillis / DAY_LENGTH);
        else dayStart.setCurrentValue((float) dayStartMillis / DAY_LENGTH);
        postInvalidate();
    }

    /**
     * Calculate the sunrise time, in milliseconds. Returned values
     * will not range beyond a 24 hour period.
     *
     * @return The sunrise time, in milliseconds.
     */
    public long getSunrise() {
        return (long) (dayStart.getTargetValue() * DAY_LENGTH);
    }

    /**
     * Set the sunset time, in milliseconds. Values can range
     * beyond the period of a day; they are modulated by a 24 hour
     * period. Change in values will not be animated.
     *
     * @param dayEndMillis              The sunset time, in milliseconds.
     */
    public void setSunset(long dayEndMillis) {
        setSunset(dayEndMillis, false);
    }

    /**
     * Set the sunset time, in milliseconds. Values can range
     * beyond the period of a day; they are modulated by a 24 hour
     * period.
     *
     * @param dayEndMillis              The sunset time, in milliseconds.
     * @param animate                   Whether to animate the change in
     *                                  values.
     */
    public void setSunset(long dayEndMillis, boolean animate) {
        dayEndMillis %= DAY_LENGTH;
        if (animate)
            dayEnd.setTargetValue((float) dayEndMillis / DAY_LENGTH);
        else dayEnd.setCurrentValue((float) dayEndMillis / DAY_LENGTH);
        postInvalidate();
    }

    /**
     * Calculate the sunset time, in milliseconds. Returned values
     * will not range beyond a 24 hour period.
     *
     * @return The sunset time, in milliseconds.
     */
    public long getSunset() {
        return (long) (dayEnd.getTargetValue() * DAY_LENGTH);
    }

    /**
     * Specify an interface to receive updates when the sunrise/sunset
     * times are modified by the user. Methods in this interface are only
     * called when the view is interacted with; calling setSunset or
     * setSunrise will not result in this interface being notified.
     *
     * @param listener                  An interface to receive updates
     *                                  when the sunrise/sunset times
     *                                  are modified.
     */
    public void setListener(SunriseListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        dayStart.updateValue(true);
        dayEnd.updateValue(true);

        float scaleX = getWidth() / 23f;
        float scaleY = getHeight() / 2f;
        float interval = (dayEnd.getDrawnValue() - dayStart.getDrawnValue()) / 2;
        float interval2 = (1 - dayEnd.getDrawnValue() + dayStart.getDrawnValue()) / 2;
        float start = dayStart.getDrawnValue() - (1 - dayEnd.getDrawnValue() + dayStart.getDrawnValue());
        interval *= 24 * scaleX;
        interval2 *= 24 * scaleX;
        start *= 24 * scaleX;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        Path path = new Path();
        path.moveTo(start, scaleY);
        path.rQuadTo(interval2, scaleY * ((interval2 / interval + 1) / 2), interval2 * 2, 0);
        path.rQuadTo(interval, -scaleY * ((interval / interval2 + 1) / 2), interval * 2, 0);
        path.rQuadTo(interval2, scaleY * ((interval2 / interval + 1) / 2), interval2 * 2, 0);
        path.rQuadTo(interval, -scaleY * ((interval / interval2 + 1) / 2), interval * 2, 0);

        canvas.clipPath(path);
        canvas.drawRect(0, 0, (int) scaleX * hour, (int) scaleY, sunrisePaint);
        canvas.drawRect(0, (int) scaleY, (int) scaleX * hour, getHeight(), sunsetPaint);
        canvas.drawRect((int) scaleX * hour, 0, getWidth(), getHeight(), linePaint);

        if (!dayStart.isTargetValue() || !dayEnd.isTargetValue())
            postInvalidate();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float horizontalDistance = event.getX() / getWidth();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                moveBeginStart = null;
                moveBeginEnd = null;
                if (!dayStart.isTargetValue() || !dayEnd.isTargetValue())
                    break;
                if (Math.abs(horizontalDistance - dayStart.getDrawnValue()) < Math.abs(horizontalDistance - dayEnd.getDrawnValue()))
                    moveBeginStart = dayStart.getDrawnValue() - horizontalDistance;
                else moveBeginEnd = dayEnd.getDrawnValue() - horizontalDistance;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (moveBeginStart != null)
                    dayStart.setTargetValue(Math.min(Math.min(dayEnd.getTargetValue() - DAY_HOUR, dayEnd.getTargetValue() - min),
                            Math.max(Math.max(DAY_START, dayEnd.getTargetValue() - max), moveBeginStart + horizontalDistance)));
                else if (moveBeginEnd != null)
                    dayEnd.setTargetValue(Math.min(Math.min(DAY_END, dayStart.getTargetValue() + max), Math.max(dayStart.getTargetValue() + DAY_HOUR,
                            Math.max(moveBeginEnd + horizontalDistance, dayStart.getTargetValue() + min))));
                if (getParent() != null)
                    getParent().requestDisallowInterceptTouchEvent(true);
                postInvalidate();
            }
            case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (listener != null) {
                    if (moveBeginStart != null)
                        listener.onSunriseChanged(this, getSunrise());
                    else if (moveBeginEnd != null)
                        listener.onSunsetChanged(this, getSunset());
                }
                if (getParent() != null)
                    getParent().requestDisallowInterceptTouchEvent(false);
                moveBeginStart = null;
                moveBeginEnd = null;
            }
        }
        return false;
    }

    public interface SunriseListener {
        void onSunriseChanged(SunriseSunsetView view, long sunriseMillis);
        void onSunsetChanged(SunriseSunsetView view, long sunsetMillis);
    }
}