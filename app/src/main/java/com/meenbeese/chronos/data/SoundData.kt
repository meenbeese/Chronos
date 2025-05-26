package com.meenbeese.chronos.data

import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable

import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.utils.AudioUtils
import com.meenbeese.chronos.utils.Option

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class SoundData(
    val name: String,
    val type: String,
    val url: String
) : Parcelable {

    @IgnoredOnParcel
    private var ringtone: Option<Ringtone> = Option.None

    constructor(name: String, type: String, url: String, ringtone: Ringtone?) : this(name, type, url) {
        this.ringtone = if (ringtone != null) Option.Some(ringtone) else Option.None
    }

    /**
     * Plays the sound. This will pass the SoundData instance to the provided
     * [Chronos](../Chronos) class, which will store the currently playing sound
     * until it is stopped or cancelled.
     *
     * @param chronos           The active Application instance.
     */
    @UnstableApi
    fun play(chronos: Chronos) {
        if (type == TYPE_RINGTONE && url.startsWith("content://")) {
            if (ringtone.isEmpty()) {
                ringtone = Option.Some(
                    RingtoneManager.getRingtone(chronos, Uri.parse(url)).apply {
                        audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    }
                )
            }
            ringtone.map { AudioUtils.playRingtone(it) }
        } else {
            AudioUtils.playStream(
                url, type,
                androidx.media3.common.AudioAttributes.Builder()
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
     * @param chronos           The active Application instance.
     */
    @UnstableApi
    fun stop(chronos: Chronos) {
        ringtone.map { it.stop() }.takeIf { ringtone.isDefined() } ?: AudioUtils.stopStream()
    }

    /**
     * Preview the sound on the "media" volume channel.
     *
     * @param chronos           The active Application instance.
     */
    @UnstableApi
    fun preview(chronos: Chronos) {
        if (url.startsWith("content://")) {
            if (ringtone.isEmpty()) {
                ringtone = Option.Some(
                    RingtoneManager.getRingtone(chronos, Uri.parse(url)).apply {
                        audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    }
                )
            }
            ringtone.map { AudioUtils.playRingtone(it) }
        } else {
            AudioUtils.playStream(
                url, type,
                androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(C.USAGE_ALARM)
                    .build()
            )
        }
    }

    /**
     * Decide whether the sound is currently playing or not.
     *
     * @param chronos           The active Application instance.
     * @return                  True if "this" sound is playing.
     */
    @UnstableApi
    fun isPlaying(chronos: Chronos): Boolean {
        return ringtone.map { it.isPlaying }.getOrElse(AudioUtils.isPlayingStream(url))
    }

    /**
     * Sets the player volume to the given float.
     *
     * @param chronos           The active Application instance.
     * @param volume            The volume between 0 and 1
     */
    @UnstableApi
    fun setVolume(chronos: Chronos, volume: Float) {
        ringtone.map {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                it.volume = volume
            } else {
                throw IllegalArgumentException("Attempted to set the ringtone volume on a device older than Android P.")
            }
        }.takeIf { ringtone.isDefined() } ?: AudioUtils.setStreamVolume(volume)
    }

    val isSetVolumeSupported: Boolean
        /**
         * Is the setVolume method supported on this version of Android
         *
         * @return true if supported
         */
        get() = ringtone is Option.None || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

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
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + (ringtone.map { it.hashCode() }.getOrElse(0))
        return result
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
            if (string.contains(SEPARATOR)) {
                val data = string.split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (data.size == 3 && data.all { it.isNotEmpty() }) {
                    return Option.Some(SoundData(data[0], data[1], data[2]))
                }
            }
            return Option.None
        }
    }
}
