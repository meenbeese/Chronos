package com.meenbeese.chronos.utils

import android.content.Context
import android.media.Ringtone
import android.widget.Toast

import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

@UnstableApi
class AudioUtils(private val context: Context) : Player.Listener {
    private var player: ExoPlayer? = null
    private var currentStream: String? = null
    private var currentRingtone: Ringtone? = null

    private val isRingtonePlaying: Boolean
        get() = currentRingtone?.isPlaying == true

    init {
        initializePlayer()
    }

    private fun initializePlayer(attributes: AudioAttributes? = null) {
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
        if (!ringtone.isPlaying) {
            ringtone.play()
        }
        currentRingtone = ringtone
    }

    fun playStream(url: String, type: String = "auto", attributes: AudioAttributes? = null) {
        stopCurrentSound()
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
}
