package com.meenbeese.chronos.data

import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build

import com.google.android.exoplayer2.C
import com.meenbeese.chronos.Chronos


data class SoundData(val name: String, val type: String, val url: String) {
    private var ringtone: Ringtone? = null

    constructor(name: String, type: String, url: String, ringtone: Ringtone?) : this(
        name,
        type,
        url
    ) {
        this.ringtone = ringtone
    }

    /**
     * Plays the sound. This will pass the SoundData instance to the provided
     * [Chronos](../Chronos) class, which will store the currently playing sound
     * until it is stopped or cancelled.
     *
     * @param chronos           The active Application instance.
     */
    fun play(chronos: Chronos) {
        if (type == TYPE_RINGTONE && url.startsWith("content://")) {
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(chronos, Uri.parse(url))
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            }
            chronos.playRingtone(ringtone)
        } else {
            chronos.playStream(
                url,
                com.google.android.exoplayer2.audio.AudioAttributes.Builder()
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
    fun stop(chronos: Chronos) {
        if (ringtone != null) ringtone!!.stop() else chronos.stopStream()
    }

    /**
     * Preview the sound on the "media" volume channel.
     *
     * @param chronos           The active Application instance.
     */
    fun preview(chronos: Chronos) {
        if (url.startsWith("content://")) {
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(chronos, Uri.parse(url))
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            }
            chronos.playRingtone(ringtone)
        } else {
            chronos.playStream(
                url,
                com.google.android.exoplayer2.audio.AudioAttributes.Builder()
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
    fun isPlaying(chronos: Chronos): Boolean {
        return if (ringtone != null) ringtone!!.isPlaying else chronos.isPlayingStream(url)
    }

    /**
     * Sets the player volume to the given float.
     *
     * @param chronos           The active Application instance.
     * @param volume            The volume between 0 and 1
     */
    fun setVolume(chronos: Chronos, volume: Float) {
        if (ringtone != null) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.volume = volume
        } else {
            // Not possible
            throw IllegalArgumentException("Attempted to set the ringtone volume on a device older than Android P.")
        } else chronos.setStreamVolume(volume)
    }

    val isSetVolumeSupported: Boolean
        /**
         * Is the setVolume method supported on this version of Android
         *
         * @return true if supported
         */
        get() = ringtone == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    companion object {
        private const val SEPARATOR = ":ChronosSoundData:"
        const val TYPE_RINGTONE = "ringtone"

        /**
         * Construct a new instance of SoundData from an identifier string which was
         * (hopefully) created by [toString](#tostring).
         *
         * @param string            A non-null identifier string.
         * @return                  A recreated SoundData instance.
         */
        @JvmStatic
        fun fromString(string: String): SoundData? {
            if (string.contains(SEPARATOR)) {
                val data = string.split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (data.size == 3 && data[0].isNotEmpty() && data[1].isNotEmpty() && data[2].isNotEmpty()) return SoundData(
                    data[0], data[1], data[2]
                )
            }
            return null
        }
    }
}
