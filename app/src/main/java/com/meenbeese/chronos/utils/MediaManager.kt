package com.meenbeese.chronos.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast

import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.AudioAttributes as M3AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.utils.MediaUtils.areHeadphonesConnected

import arrow.core.None
import arrow.core.Option
import arrow.core.Some

@UnstableApi
class MediaManager(private val context: Context) : Player.Listener {
    private var player: ExoPlayer? = null
    private var currentStream: String? = null
    private var currentRingtone: Ringtone? = null
    private var currentRingtoneUrl: String? = null

    private val isRingtonePlaying: Boolean
        get() = currentRingtone?.isPlaying == true

    private val onlyPlayOverHeadphones = Preferences.PLAY_ON_HEADPHONES.get(context)

    init {
        initializePlayer()
    }

    private fun initializePlayer(attributes: M3AudioAttributes? = null) {
        player?.release()
        player = ExoPlayer.Builder(context)
            .apply {
                attributes?.let {
                    setAudioAttributes(it, true)
                }
            }
            .build().also {
                it.addListener(this)
            }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING, Player.STATE_READY, Player.STATE_IDLE -> {}
            else -> currentStream = null
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        currentStream = null
        error.cause?.printStackTrace()
        Toast.makeText(
            context,
            "${error.cause?.javaClass?.simpleName}: ${error.cause?.message}",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun isPlayingStream(url: String): Boolean {
        return currentStream != null && currentStream == url
    }

    fun stopCurrentSound() {
        if (isRingtonePlaying) {
            currentRingtone?.stop()
        }
        stopStream()
        currentRingtoneUrl = null
    }

    fun stopStream() {
        player?.stop()
        currentStream = null
    }

    fun setStreamVolume(volume: Float) {
        player?.volume = volume
    }

    fun playRingtone(ringtone: Ringtone) {
        stopCurrentSound()
        if (onlyPlayOverHeadphones && !areHeadphonesConnected(context)) {
            Toast.makeText(context, "Please connect headphones to play audio", Toast.LENGTH_SHORT).show()
            return
        }
        if (!ringtone.isPlaying) {
            ringtone.play()
        }
        currentRingtone = ringtone
    }

    fun playStream(url: String, type: String = "auto", attributes: M3AudioAttributes? = null) {
        stopCurrentSound()

        if (onlyPlayOverHeadphones && !areHeadphonesConnected(context)) {
            Toast.makeText(context, "Please connect headphones to play audio", Toast.LENGTH_SHORT).show()
            return
        }

        if (attributes != null) {
            initializePlayer(attributes)
        }

        val uri = url.toUri()
        val mediaItem = MediaItem.fromUri(uri)

        val dataSourceFactory = DefaultDataSource.Factory(context)

        val mediaSource: MediaSource = when {
            type.equals("hls", ignoreCase = true) || url.endsWith(".m3u8", ignoreCase = true) -> {
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }
            else -> {
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }
        }

        player?.apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }

        currentStream = url
    }

    fun play(sound: SoundData) {
        if (sound.type == TYPE_RINGTONE && sound.url.startsWith("content://")) {
            val ringtone = RingtoneManager.getRingtone(context, sound.url.toUri()).apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            }
            currentRingtoneUrl = sound.url
            playRingtone(ringtone)
        } else {
            playStream(
                sound.url, sound.type,
                M3AudioAttributes.Builder()
                    .setUsage(C.USAGE_ALARM)
                    .build()
            )
        }
    }

    fun stop(sound: SoundData?) {
        if (sound == null) return
        stopCurrentSound()
    }

    fun isPlaying(sound: SoundData): Boolean {
        return if (sound.type == TYPE_RINGTONE) {
            currentRingtoneUrl == sound.url && isRingtonePlaying
        } else {
            isPlayingStream(sound.url)
        }
    }

    fun setVolume(sound: SoundData, volume: Float) {
        setStreamVolume(volume)
    }

    val isSetVolumeSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    fun getCurrentPosition(url: String): Long {
        return if (currentStream == url) {
            player?.currentPosition ?: 0L
        } else 0L
    }

    fun getDuration(url: String): Long {
        return if (currentStream == url) {
            val dur = player?.duration ?: 0L
            if (dur == C.TIME_UNSET) 0L else dur
        } else 0L
    }

    companion object {
        private const val SEPARATOR = ":ChronosSoundData:"
        const val TYPE_RINGTONE: String = "ringtone"

        fun encode(sound: SoundData): String {
            return sound.name + SEPARATOR + sound.type + SEPARATOR + sound.url
        }

        fun decode(string: String): Option<SoundData> {
            val data = string.split(SEPARATOR).takeIf { it.size == 3 } ?: return None
            val (name, type, url) = data
            return Some(SoundData(name, type, url))
        }
    }
}
