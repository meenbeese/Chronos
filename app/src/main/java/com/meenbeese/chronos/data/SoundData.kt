package com.meenbeese.chronos.data

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Parcelable

import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.AudioAttributes as M3AudioAttributes

import arrow.core.Option
import arrow.core.None
import arrow.core.Some

import com.meenbeese.chronos.utils.AudioManager

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Parcelize
@UnstableApi
class SoundData(
    val name: String,
    val type: String,
    val url: String
) : Parcelable, KoinComponent {

    @IgnoredOnParcel
    private val audioUtils: AudioManager by inject()

    /**
     * Plays the sound. This will pass the SoundData instance to the provided
     * [Chronos](../Chronos) class, which will store the currently playing sound
     * until it is stopped or cancelled.
     *
     * @param context           The active Application instance.
     */
    fun play(context: Context) {
        if (type == TYPE_RINGTONE && url.startsWith("content://")) {
            val ringtone = RingtoneManager.getRingtone(context, url.toUri()).apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            }
            audioUtils.playRingtone(ringtone)
        } else {
            audioUtils.playStream(
                url, type,
                M3AudioAttributes.Builder()
                    .setUsage(C.USAGE_ALARM)
                    .build()
            )
        }
    }

    /**
     * Stops the currently playing alarm. This only differentiates between sounds
     * if the sound is a ringtone; if it is a stream, then all streams will be stopped,
     * regardless of whether this sound is in fact the currently playing stream or not.
     *
     */
    fun stop() = audioUtils.stopStream()

    /**
     * Decide whether the sound is currently playing or not.
     *
     * @return                  True if "this" sound is playing.
     */
    fun isPlaying() = audioUtils.isPlayingStream(url)

    /**
     * Sets the player volume to the given float.
     *
     * @param volume            The volume between 0 and 1
     */
    fun setVolume(volume: Float) = audioUtils.setStreamVolume(volume)

    val isSetVolumeSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    /**
     * Returns an identifier string that can be used to recreate this
     * SoundDate class.
     *
     * @return                  A non-null identifier string.
     */
    override fun toString(): String {
        return name + SEPARATOR + type + SEPARATOR + url
    }

    /**
     * Decide if two SoundData are equal.
     *
     * @param other               The object to compare to.
     * @return                  True if the SoundData contain the same sound.
     */
    override fun equals(other: Any?): Boolean {
        return (other is SoundData && other.url == url)
    }

    override fun hashCode(): Int {
        return 31 * (31 * (31 * name.hashCode() + type.hashCode()) + url.hashCode())
    }

    companion object {
        private const val SEPARATOR = ":ChronosSoundData:"
        const val TYPE_RINGTONE: String = "ringtone"

        /**
         * Construct a new instance of SoundData from an identifier string which was
         * (hopefully) created by [toString](#tostring).
         *
         * @param string            A non-null identifier string.
         * @return                  A recreated SoundData instance.
         */
        @JvmStatic
        fun fromString(string: String): Option<SoundData> {
            val data = string.split(SEPARATOR).takeIf { it.size == 3 } ?: return None
            val (name, type, url) = data
            return Some(SoundData(name, type, url))
        }
    }
}
