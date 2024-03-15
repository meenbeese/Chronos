package com.meenbeese.chronos.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

import androidx.annotation.DrawableRes

import com.meenbeese.chronos.R

import kotlin.math.min


class AppIconView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private lateinit var bgBitmap: Bitmap
    private lateinit var fgSecondsBitmap: Bitmap
    private lateinit var fgMinutesBitmap: Bitmap
    private lateinit var fgHoursBitmap: Bitmap
    private lateinit var fgBitmap: Bitmap
    private lateinit var path: Path

    private val paint: Paint = Paint()
    private var animator: ValueAnimator
    private var size = 0
    private var bgRotation = 1f
    private var rotation = 1f
    private var bgScale = 0f
    private var fgScale = 0f

    init {
        paint.isAntiAlias = true
        paint.color = Color.parseColor("#212121")
        paint.isDither = true
        animator = ValueAnimator.ofFloat(bgScale, 0.8f)
        animator.interpolator = OvershootInterpolator()
        animator.duration = 750
        animator.startDelay = 250
        animator.addUpdateListener { animator: ValueAnimator ->
            bgScale = animator.animatedValue as Float
            invalidate()
        }
        animator.start()
        animator = ValueAnimator.ofFloat(rotation, 0f)
        animator.interpolator = DecelerateInterpolator()
        animator.duration = 1000
        animator.startDelay = 250
        animator.addUpdateListener { animator: ValueAnimator ->
            fgScale = animator.animatedFraction * 0.8f
            rotation = animator.animatedValue as Float
            invalidate()
        }
        animator.start()
        animator = ValueAnimator.ofFloat(bgRotation, 0f)
        animator.interpolator = DecelerateInterpolator()
        animator.duration = 1250
        animator.startDelay = 250
        animator.addUpdateListener { animator: ValueAnimator ->
            bgRotation = animator.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    private fun getBitmap(size: Int, @DrawableRes resource: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inScaled = false
        return ThumbnailUtils.extractThumbnail(
            BitmapFactory.decodeResource(
                resources,
                resource,
                options
            ), size, size
        )
    }

    private fun getFgMatrix(bitmap: Bitmap): Matrix {
        val matrix = Matrix()
        matrix.postTranslate(-bitmap.width.toFloat() / 2, -bitmap.height.toFloat() / 2)
        matrix.postScale(fgScale, fgScale)
        return matrix
    }

    override fun onDraw(canvas: Canvas) {
        val size = min(width, height)
        if (this.size != size) {
            this.size = size
            bgBitmap = getBitmap(size, R.mipmap.ic_launcher_bg)
            fgSecondsBitmap = getBitmap(size, R.mipmap.ic_launcher_fg_seconds)
            fgMinutesBitmap = getBitmap(size, R.mipmap.ic_launcher_fg_minutes)
            fgHoursBitmap = getBitmap(size, R.mipmap.ic_launcher_fg_hours)
            fgBitmap = getBitmap(size, R.mipmap.ic_launcher_fg)
            path = Path()
            path.arcTo(RectF(0f, 0f, size.toFloat(), size.toFloat()), 0f, 359f)
            path.close()
        }
        var matrix = Matrix()
        matrix.postScale(
            bgScale * 0.942986f,
            bgScale * 0.942986f,
            size.toFloat() / 2,
            size.toFloat() / 2
        )
        val path = Path()
        this.path.transform(matrix, path)
        canvas.drawPath(path, paint)
        matrix = getFgMatrix(bgBitmap)
        matrix.postRotate(bgRotation * 120)
        matrix.postTranslate(bgBitmap.width.toFloat() / 2, bgBitmap.height.toFloat() / 2)
        canvas.drawBitmap(bgBitmap, matrix, paint)
        matrix = getFgMatrix(fgSecondsBitmap)
        matrix.postRotate(-rotation * 720)
        matrix.postTranslate(
            fgSecondsBitmap.width.toFloat() / 2,
            fgSecondsBitmap.height.toFloat() / 2
        )
        canvas.drawBitmap(fgSecondsBitmap, matrix, paint)
        matrix = getFgMatrix(fgMinutesBitmap)
        matrix.postRotate(-rotation * 360)
        matrix.postTranslate(
            fgMinutesBitmap.width.toFloat() / 2,
            fgMinutesBitmap.height.toFloat() / 2
        )
        canvas.drawBitmap(fgMinutesBitmap, matrix, paint)
        matrix = getFgMatrix(fgHoursBitmap)
        matrix.postRotate(-rotation * 60)
        matrix.postTranslate(
            fgHoursBitmap.width.toFloat() / 2,
            fgHoursBitmap.height.toFloat() / 2
        )
        canvas.drawBitmap(fgHoursBitmap, matrix, paint)
        matrix = getFgMatrix(fgBitmap)
        matrix.postTranslate(fgBitmap.width.toFloat() / 2, fgBitmap.height.toFloat() / 2)
        canvas.drawBitmap(fgBitmap, matrix, paint)
    }

    fun addListener(listener: Animator.AnimatorListener?) {
        animator.addListener(listener)
    }
}
