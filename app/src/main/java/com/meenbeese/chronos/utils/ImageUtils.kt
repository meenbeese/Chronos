package com.meenbeese.chronos.utils

import android.net.Uri
import android.widget.ImageView

import com.bumptech.glide.Glide

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
        if (backgroundUrl != null && backgroundUrl.isNotEmpty()) {
            if (backgroundUrl.startsWith("http")) Glide.with(imageView.context).load(backgroundUrl)
                .into(imageView) else if (backgroundUrl.contains("://")) {
                if (backgroundUrl.startsWith("content://")) {
                    var path = Uri.parse(backgroundUrl).lastPathSegment
                    path = if (path != null && path.contains(":")) "/storage/" + path.replaceFirst(
                        ":".toRegex(),
                        "/"
                    ) else Uri.parse(backgroundUrl).path
                    Glide.with(imageView.context).load(File(path!!)).into(imageView)
                } else Glide.with(imageView.context).load(Uri.parse(backgroundUrl)).into(imageView)
            } else Glide.with(imageView.context).load(File(backgroundUrl)).into(imageView)
        }
    }
}
