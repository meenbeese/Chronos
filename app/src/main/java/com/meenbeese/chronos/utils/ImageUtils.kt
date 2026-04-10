package com.meenbeese.chronos.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.get

import arrow.core.Either
import arrow.core.getOrElse

import coil3.compose.rememberAsyncImagePainter
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.ext.getFlow

import java.io.File

object ImageUtils {
    sealed interface ClockBackground {
        data class Image(
            val painter: Painter,
            val requestData: Any
        ) : ClockBackground

        data class Solid(val color: Color) : ClockBackground

        data object None : ClockBackground
    }

    private sealed interface ImageSource {
        data class DrawableRes(val resId: Int) : ImageSource
        data class RemoteOrContent(val url: String) : ImageSource
        data class FilePath(val file: File) : ImageSource
    }

    private fun resolveImageSource(context: Context, url: String): ImageSource {
        return when {
            url.startsWith("drawable/") -> {
                val resName = url.removePrefix("drawable/")
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                ImageSource.DrawableRes(if (resId != 0) resId else R.drawable.snowytrees)
            }
            url.startsWith("http") || url.startsWith("content://") -> {
                ImageSource.RemoteOrContent(url)
            }
            url.isNotEmpty() -> {
                ImageSource.FilePath(File(url))
            }
            else -> ImageSource.DrawableRes(R.drawable.snowytrees)
        }
    }

    @Composable
    private fun painterFor(source: ImageSource): Painter {
        return when (source) {
            is ImageSource.DrawableRes -> painterResource(id = source.resId)
            is ImageSource.RemoteOrContent -> rememberAsyncImagePainter(model = source.url)
            is ImageSource.FilePath -> rememberAsyncImagePainter(model = Uri.fromFile(source.file))
        }
    }

    private fun requestDataFor(source: ImageSource): Any {
        return when (source) {
            is ImageSource.DrawableRes -> source.resId
            is ImageSource.RemoteOrContent -> source.url
            is ImageSource.FilePath -> source.file
        }
    }

    @Composable
    fun getBackgroundPainter(isAlarm: Boolean): Painter? {
        return when (val background = rememberClockBackground(isAlarm)) {
            is ClockBackground.Image -> background.painter
            is ClockBackground.Solid -> ColorPainter(background.color)
            is ClockBackground.None -> null
        }
    }

    @Composable
    fun rememberBackgroundPainterState(isAlarm: Boolean): Painter? {
        return when (val background = rememberClockBackground(isAlarm)) {
            is ClockBackground.Image -> background.painter
            is ClockBackground.Solid -> ColorPainter(background.color)
            is ClockBackground.None -> null
        }
    }

    @Composable
    fun rememberClockBackground(isAlarm: Boolean): ClockBackground {
        val context = LocalContext.current

        val colorfulBg by Preferences.COLORFUL_BACKGROUND.getFlow(context).collectAsState(initial = false)
        val bgColor by Preferences.BACKGROUND_COLOR.getFlow(context).collectAsState(initial = 0)
        val bgImage by Preferences.BACKGROUND_IMAGE.getFlow(context).collectAsState(initial = "")
        val allowRingingBg by Preferences.RINGING_BACKGROUND_IMAGE.getFlow(context).collectAsState(initial = true)

        if (isAlarm && !allowRingingBg) return ClockBackground.None

        return if (colorfulBg) {
            ClockBackground.Solid(Color(bgColor))
        } else {
            val source = resolveImageSource(context, bgImage)
            ClockBackground.Image(
                painter = painterFor(source),
                requestData = requestDataFor(source)
            )
        }
    }

    fun isBitmapDark(bitmap: Bitmap, sampleSize: Int = 10): Boolean {
        var darkPixels = 0
        var totalPixels = 0

        val stepX = bitmap.width / sampleSize
        val stepY = bitmap.height / sampleSize

        for (x in 0 until bitmap.width step stepX) {
            for (y in 0 until bitmap.height step stepY) {
                val color = bitmap[x, y]
                if (isColorDark(color)) {
                    darkPixels++
                }
                totalPixels++
            }
        }
        return darkPixels >= totalPixels / 2
    }

    fun isColorDark(colorInt: Int): Boolean {
        val color = Color(colorInt)
        val darkness = 1 - (
            0.299 * color.red +
            0.587 * color.green +
            0.114 * color.blue
        )
        return darkness >= 0.5
    }

    suspend fun getContrastingTextColorFromBg(
        context: Context,
        background: ClockBackground
    ): Color {
        return when (background) {
            is ClockBackground.None -> Color.DarkGray
            is ClockBackground.Solid -> {
                if (isColorDark(background.color.toArgb())) Color.LightGray else Color.DarkGray
            }
            is ClockBackground.Image -> {
                val result: Either<Throwable, Color> = Either.catch {
                    val imageRequest = ImageRequest.Builder(context)
                        .data(background.requestData)
                        .size(200, 200)
                        .allowHardware(false)
                        .build()

                    val drawable = context.imageLoader.execute(imageRequest).image
                    val bitmap = drawable?.toBitmap()
                    val isDark = bitmap?.let { isBitmapDark(it) } ?: false

                    if (isDark) Color.LightGray else Color.DarkGray
                }

                result.getOrElse { Color.DarkGray }
            }
        }
    }
}
