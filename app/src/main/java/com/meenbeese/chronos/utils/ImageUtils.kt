package com.meenbeese.chronos.utils

import android.net.Uri
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meenbeese.chronos.data.PreferenceData

import java.io.File


object ImageUtils {
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
            imageUri?.let { uri -> loadImageWithGlide(imageView, uri) }
        }
    }

    private fun loadImageWithGlide(imageView: ImageView, uri: Uri) {
        Glide.with(imageView.context)
            .load(uri)
            .apply(RequestOptions().centerCrop())
            .into(imageView)
    }
}
