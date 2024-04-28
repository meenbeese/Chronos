package com.meenbeese.chronos.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.meenbeese.chronos.interfaces.SlideActionListener;
import com.meenbeese.chronos.utils.DimenUtils;
import com.meenbeese.chronos.utils.ImageUtils;

import me.jfenn.androidutils.anim.AnimatedFloat;

import java.util.HashMap;
import java.util.Map;


public class SlideActionView extends View implements View.OnTouchListener {

    private float position = -1;
    private AnimatedFloat selected;
    private Map<Float, AnimatedFloat> ripples;

    private int handleRadius;
    private int expandedHandleRadius;
    private int selectionRadius;
    private int rippleRadius;

    private Paint normalPaint;
    private Paint outlinePaint;
    private Paint bitmapPaint;

    private Bitmap leftImage, rightImage;

    private SlideActionListener listener;

    public SlideActionView(Context context) {
        super(context);
        init();
    }

    public SlideActionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        handleRadius = DimenUtils.INSTANCE.dpToPx(12);
        expandedHandleRadius = DimenUtils.INSTANCE.dpToPx(32);
        selectionRadius = DimenUtils.INSTANCE.dpToPx(42);
        rippleRadius = DimenUtils.INSTANCE.dpToPx(140);

        selected = new AnimatedFloat(0);
        ripples = new HashMap<>();

        normalPaint = new Paint();
        normalPaint.setStyle(Paint.Style.FILL);
        normalPaint.setColor(Color.GRAY);
        normalPaint.setAntiAlias(true);
        normalPaint.setDither(true);

        outlinePaint = new Paint();
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(Color.GRAY);
        outlinePaint.setAntiAlias(true);
        outlinePaint.setDither(true);

        bitmapPaint = new Paint();
        bitmapPaint.setStyle(Paint.Style.FILL);
        bitmapPaint.setColor(Color.GRAY);
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setDither(true);
        bitmapPaint.setFilterBitmap(true);

        setOnTouchListener(this);
        setFocusable(true);
        setClickable(true);
    }

    /**
     * Specify an interface to pass events to when an action
     * is selected.
     *
     * @param listener          An interface to pass events to.
     */
    public void setListener(SlideActionListener listener) {
        this.listener = listener;
    }

    /**
     * Specifies the icon to display on the left side of the view,
     * as a Drawable. If it is just as easier to pass a Bitmap, you
     * should avoid using this method; all it does is convert the
     * drawable to a bitmap, then call the same method again.
     *
     * @param drawable          The Drawable to use as an icon.
     */
    public void setLeftIcon(Drawable drawable) {
        setLeftIcon(ImageUtils.INSTANCE.toBitmap(drawable));
    }

    /**
     * Specifies the icon to display on the left side of the view.
     *
     * @param bitmap            The Bitmap to use as an icon.
     */
    public void setLeftIcon(Bitmap bitmap) {
        leftImage = bitmap;
        postInvalidate();
    }

    /**
     * Specifies the icon to display on the right side of the view,
     * as a Drawable. If it is just as easier to pass a Bitmap, you
     * should avoid using this method; all it does is convert the
     * drawable to a bitmap, then call the same method again.
     *
     * @param drawable          The Drawable to use as an icon.
     */
    public void setRightIcon(Drawable drawable) {
        setRightIcon(ImageUtils.INSTANCE.toBitmap(drawable));
    }

    /**
     * Specifies the icon to display on the right side of the view.
     *
     * @param bitmap            The Bitmap to use as an icon.
     */
    public void setRightIcon(Bitmap bitmap) {
        rightImage = bitmap;
        postInvalidate();
    }

    /**
     * Specify the color of the touch handle in the center of
     * the view. The alpha of this color is modified to be somewhere
     * between 0 and 150.
     *
     * @param handleColor       The color of the touch handle.
     */
    public void setTouchHandleColor(@ColorInt int handleColor) {
        normalPaint.setColor(handleColor);
    }

    /**
     * @return The color of the touch handle in the center of the view.
     */
    @ColorInt
    public int getTouchHandleColor() {
        return normalPaint.getColor();
    }

    /**
     * Specify the color of the random outlines drawn all over the place.
     *
     * @param outlineColor      The color of the random outlines.
     */
    public void setOutlineColor(@ColorInt int outlineColor) {
        outlinePaint.setColor(outlineColor);
    }

    /**
     * @return The color of the random outlines drawn all over the place.
     */
    @ColorInt
    public int getOutlineColor() {
        return outlinePaint.getColor();
    }

    /**
     * Specify the color applied to the left/right icons as a filter.
     *
     * @param iconColor         The color that the left/right icons are filtered by.
     */
    public void setIconColor(@ColorInt int iconColor) {
        bitmapPaint.setColor(iconColor);
        bitmapPaint.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
    }

    /**
     * @return The color applied to the left/right icons as a filter.
     */
    @ColorInt
    public int getIconColor() {
        return bitmapPaint.getColor();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        selected.next(true);
        position = position < 0 ? (float) getWidth() / 2 : position;

        drawCircle(canvas);
        drawImages(canvas);
        drawOutline(canvas);
        drawRipples(canvas);

        if (!selected.isTarget() || !ripples.isEmpty()) {
            postInvalidate();
        }
    }

    private void drawCircle(Canvas canvas) {
        normalPaint.setAlpha(150 - (int) (selected.val() * 100));
        int radius = (int) ((handleRadius * (1 - selected.val())) + (expandedHandleRadius * selected.val()));
        float drawnX = (position * selected.val()) + (((float) getWidth() / 2) * (1 - selected.val()));
        canvas.drawCircle(drawnX, (float) getHeight() / 2, radius, normalPaint);
    }

    private void drawImages(Canvas canvas) {
        if (leftImage != null && rightImage != null) {
            float drawnX = (position * selected.val()) + (((float) getWidth() / 2) * (1 - selected.val()));
            bitmapPaint.setAlpha((int) (255 * Math.min(1f, Math.max(0f, (getWidth() - drawnX - selectionRadius) / getWidth()))));
            canvas.drawBitmap(leftImage, selectionRadius - ((float) leftImage.getWidth() / 2), (float) (getHeight() - leftImage.getHeight()) / 2, bitmapPaint);
            bitmapPaint.setAlpha((int) (255 * Math.min(1f, Math.max(0f, (drawnX - selectionRadius) / getWidth()))));
            canvas.drawBitmap(rightImage, getWidth() - selectionRadius - ((float) leftImage.getWidth() / 2), (float) (getHeight() - leftImage.getHeight()) / 2, bitmapPaint);
        }
    }

    private void drawOutline(Canvas canvas) {
        float drawnX = (position * selected.val()) + (((float) getWidth() / 2) * (1 - selected.val()));
        if (Math.abs(((float) getWidth() / 2) - drawnX) > (float) selectionRadius / 2) {
            float progress = drawnX * 2 < getWidth() ? Math.min(1f, Math.max(0f, ((getWidth() - ((drawnX + selectionRadius) * 2)) / getWidth()))) : Math.min(1f, Math.max(0f, (((drawnX - selectionRadius) * 2) - getWidth()) / getWidth()));
            progress = (float) Math.pow(progress, 0.2f);
            outlinePaint.setAlpha((int) (255 * progress));
            float circleX = drawnX * 2 < getWidth() ? selectionRadius : getWidth() - selectionRadius;
            canvas.drawCircle(circleX, (float) getHeight() / 2, ((float) selectionRadius / 2) + (rippleRadius * (1 - progress)), outlinePaint);
        }
    }

    private void drawRipples(Canvas canvas) {
        for (float x : ripples.keySet()) {
            AnimatedFloat scale = ripples.get(x);
            assert scale != null;
            scale.next(true, 1600);
            normalPaint.setAlpha((int) (150 * (scale.getTarget() - scale.val()) / scale.getTarget()));
            canvas.drawCircle(x, (float) getHeight() / 2, scale.val(), normalPaint);
            if (scale.isTarget()) {
                ripples.remove(x);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float eventX = event.getX();
        float halfWidth = (float) getWidth() / 2;
        boolean isActionDown = event.getAction() == MotionEvent.ACTION_DOWN;
        boolean isActionUp = event.getAction() == MotionEvent.ACTION_UP;

        if (isActionDown && Math.abs(eventX - halfWidth) < selectionRadius) {
            selected.to(1f);
        } else if (isActionUp && selected.getTarget() > 0) {
            handleActionUp(eventX);
            return true;
        }

        if (selected.getTarget() > 0) {
            position = eventX;
            postInvalidate();
        }

        return false;
    }

    private void handleActionUp(float eventX) {
        selected.to(0f);
        float rippleStart = eventX > getWidth() - (selectionRadius * 2) ? getWidth() - selectionRadius : selectionRadius;
        createRipple(rippleStart);
        postInvalidate();
    }

    private void createRipple(float rippleStart) {
        AnimatedFloat ripple = new AnimatedFloat(selectionRadius);
        ripple.to((float) rippleRadius);
        ripples.put(rippleStart, ripple);

        if (listener != null) {
            if (rippleStart == selectionRadius) {
                listener.onSlideLeft();
            } else {
                listener.onSlideRight();
            }
        }
    }
}