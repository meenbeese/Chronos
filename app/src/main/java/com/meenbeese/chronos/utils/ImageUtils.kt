package com.meenbeese.chronos.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView

import coil3.load
import coil3.request.crossfade
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation

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
            if (it.bitmap != null)
                return it.bitmap
        }

        val bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0)
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        else Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)

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
        val backgroundUrl = PreferenceData.BACKGROUND_IMAGE.getValue<String>(imageView.context)
        backgroundUrl?.takeIf { it.isNotEmpty() }?.let { it ->
            val imageUri = when {
                it.startsWith("http") -> Uri.parse(it)
                it.startsWith("content://") -> {
                    val path = Uri.parse(it).lastPathSegment?.let { segment ->
                        if (segment.contains(":")) "/storage/" + segment.replaceFirst(":", "/") else Uri.parse(it).path
                    }
                    path?.let { Uri.fromFile(File(it)) }
                }
                else -> Uri.fromFile(File(it))
            }
            imageUri?.let { uri -> loadImageWithCoil(imageView, uri) }
        }
    }

    private fun loadImageWithCoil(imageView: ImageView, uri: Uri) {
        imageView.load(uri) {
            crossfade(true)
            transformations(RoundedCornersTransformation())
        }
    }
}
