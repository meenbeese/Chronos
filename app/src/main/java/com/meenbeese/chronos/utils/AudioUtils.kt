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

@UnstableApi
class AudioUtils(private val context: Context) : Player.Listener {
    private var player: ExoPlayer? = null
    private var currentStream: String? = null
    private var currentRingtone: Ringtone? = null
    private var hlsMediaSourceFactory: HlsMediaSource.Factory? = null
    private val isRingtonePlaying: Boolean
        get() = currentRingtone?.isPlaying == true

    init {
        player = ExoPlayer.Builder(context).build()
        player?.addListener(this)

        val dataSourceFactory = DefaultDataSource.Factory(context)
        hlsMediaSourceFactory = HlsMediaSource.Factory(dataSourceFactory)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING, Player.STATE_READY, Player.STATE_IDLE -> {}
            else -> currentStream = null
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        currentStream = null
        val exception: Throwable? = error.cause
        exception?.printStackTrace()
        Toast.makeText(
            context,
            exception?.javaClass?.name + ": " + exception?.message,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun isPlayingStream(url: String): Boolean {
        return currentStream != null && currentStream == url
    }

    fun stopCurrentSound() {
        if (isRingtonePlaying) currentRingtone?.stop()
        stopStream()
    }

    private fun playStream(url: String, type: String) {
        playStream(url, type, hlsMediaSourceFactory)
    }

    private fun playStream(url: String, type: String, factory: MediaSource.Factory?) {
        stopCurrentSound()

        // Create a MediaItem from the URL
        val mediaItem: MediaItem = MediaItem.fromUri(url.toUri())

        // Error handling, including when this is a progressive stream
        // rather than a HLS stream, is in onPlayerError
        player?.setMediaSource(factory!!.createMediaSource(mediaItem))
        player?.prepare()
        player?.playWhenReady = true
        currentStream = url
    }

    fun playStream(url: String, type: String, attributes: AudioAttributes?) {
        player?.stop()
        player = ExoPlayer.Builder(context)
            .setAudioAttributes(attributes!!, true)
            .build()
        playStream(url, type)
    }

    fun stopStream() {
        player?.stop()
        currentStream = null
    }

    fun setStreamVolume(volume: Float) {
        player?.volume = volume
    }

    fun playRingtone(ringtone: Ringtone) {
        if (!ringtone.isPlaying) {
            stopCurrentSound()
            ringtone.play()
        }
        currentRingtone = ringtone
    }
}
