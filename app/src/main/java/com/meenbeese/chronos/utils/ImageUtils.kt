package com.meenbeese.chronos.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView

import androidx.core.net.toUri
import androidx.core.graphics.createBitmap

import coil3.load
import coil3.request.crossfade
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData

import java.io.File

object ImageUtils {
    /**
     * Converts drawables to bitmaps.
     *
     * @return A bitmap representing the drawable asset.
     */
    fun Drawable.toBitmap() : Bitmap {
        (this as? BitmapDrawable)?.let {
            if (it.bitmap != null) {
                return it.bitmap
            }
        }

        val bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            createBitmap(1, 1)
        } else {
            createBitmap(intrinsicWidth, intrinsicHeight)
        }

        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)

        return bitmap
    }

    /**
     * Gets the current user-defined background image from SharedPreferences and applies
     * it to the passed view.
     *
     * @param imageView         The ImageView to apply the background image to.
     */
    @JvmStatic
    fun getBackgroundImage(imageView: ImageView) {
        val context = imageView.context
        val backgroundUrl = PreferenceData.BACKGROUND_IMAGE.getValue<String>(context)

        if (backgroundUrl.isNotEmpty()) {
            when {
                backgroundUrl.startsWith("drawable/") -> {
                    val resName = backgroundUrl.removePrefix("drawable/")
                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                    if (resId != 0) {
                        loadImageWithCoil(imageView, resId)
                        return
                    }
                }
                backgroundUrl.startsWith("http") -> {
                    loadImageWithCoil(imageView, backgroundUrl.toUri())
                    return
                }
                backgroundUrl.startsWith("content://") -> {
                    val uri = backgroundUrl.toUri()
                    loadImageWithCoil(imageView, uri)
                    return
                }
                else -> {
                    val file = File(backgroundUrl)
                    loadImageWithCoil(imageView, Uri.fromFile(file))
                    return
                }
            }
        }

        imageView.setImageResource(R.drawable.snowytrees)
    }

    private fun loadImageWithCoil(imageView: ImageView, uri: Uri) {
        imageView.load(uri) {
            crossfade(true)
            transformations(RoundedCornersTransformation())
        }
    }

    private fun loadImageWithCoil(imageView: ImageView, drawableRes: Int) {
        imageView.load(drawableRes) {
            crossfade(true)
            transformations(RoundedCornersTransformation())
        }
    }
}
