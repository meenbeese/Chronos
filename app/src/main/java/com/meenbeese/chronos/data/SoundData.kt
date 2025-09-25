package com.meenbeese.chronos.data

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
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
import arrow.core.getOrElse

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

    @IgnoredOnParcel
    private var ringtone: Option<Ringtone> = None

    constructor(name: String, type: String, url: String, ringtone: Ringtone?) : this(name, type, url) {
        this.ringtone = if (ringtone != null) Some(ringtone) else None
    }

    /**
     * Plays the sound. This will pass the SoundData instance to the provided
     * [Chronos](../Chronos) class, which will store the currently playing sound
     * until it is stopped or cancelled.
     *
     * @param context           The active Application instance.
     */
    @UnstableApi
    fun play(context: Context) {
        if (type == TYPE_RINGTONE && url.startsWith("content://")) {
            if (ringtone is None) {
                ringtone = Some(
                    RingtoneManager.getRingtone(context, url.toUri()).apply {
                        audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    }
                )
            }
            ringtone.map { audioUtils.playRingtone(it) }
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
    @UnstableApi
    fun stop() {
        ringtone.map { it.stop() }.getOrElse { audioUtils.stopStream() }
    }

    /**
     * Preview the sound on the "media" volume channel.
     *
     * @param context           The active Application instance.
     */
    @UnstableApi
    fun preview(context: Context) {
        if (url.startsWith("content://")) {
            if (ringtone is None) {
                ringtone = Some(
                    RingtoneManager.getRingtone(context, url.toUri()).apply {
                        audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    }
                )
            }
            ringtone.map { audioUtils.playRingtone(it) }
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
     * Decide whether the sound is currently playing or not.
     *
     * @return                  True if "this" sound is playing.
     */
    @UnstableApi
    fun isPlaying(): Boolean {
        return ringtone.map { it.isPlaying }.getOrElse { audioUtils.isPlayingStream(url) }
    }

    /**
     * Sets the player volume to the given float.
     *
     * @param volume            The volume between 0 and 1
     */
    @UnstableApi
    fun setVolume(volume: Float) {
        ringtone.map {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                it.volume = volume
            } else {
                throw IllegalArgumentException("Attempted to set the ringtone volume on a device older than Android P.")
            }
        }.getOrElse { audioUtils.setStreamVolume(volume) }
    }

    val isSetVolumeSupported: Boolean
        /**
         * Is the setVolume method supported on this version of Android
         *
         * @return true if supported
         */
        get() = ringtone is None || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

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
        return 31 * (31 * (31 * name.hashCode() + type.hashCode()) + url.hashCode()) + ringtone.hashCode()
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
