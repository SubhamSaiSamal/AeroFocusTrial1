package com.aerofocus.app.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import androidx.annotation.OptIn
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper around Media3 [ExoPlayer] for ambient audio during focus flights.
 *
 * ## Key behaviors
 * - **Gapless looping**: Uses [Player.REPEAT_MODE_ALL] for seamless loops.
 * - **Audio focus**: Correctly handles phone calls and other interruptions.
 *   Ducks volume on transient focus loss, pauses on full loss, resumes on regain.
 * - **Volume control**: Exposes [setVolume] for the frosted-glass UI slider.
 *
 * ## Usage
 * ```kotlin
 * audioPlayer.play(R.raw.cabin_noise)
 * audioPlayer.setVolume(0.7f)
 * audioPlayer.stop()
 * ```
 */
@Singleton
class AmbientAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var currentVolume: Float = 0.8f
    private var wasPlayingBeforeFocusLoss: Boolean = false
    private var currentRawResId: Int? = null

    // ── Audio Focus ─────────────────────────────────────────────────

    private val audioFocusRequest: AudioFocusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { focusChange ->
                handleAudioFocusChange(focusChange)
            }
            .build()
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Regained focus — restore volume and resume if we were playing
                exoPlayer?.volume = currentVolume
                if (wasPlayingBeforeFocusLoss) {
                    exoPlayer?.play()
                    wasPlayingBeforeFocusLoss = false
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss — stop playback entirely
                wasPlayingBeforeFocusLoss = exoPlayer?.isPlaying == true
                exoPlayer?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporary loss (e.g., phone call) — pause
                wasPlayingBeforeFocusLoss = exoPlayer?.isPlaying == true
                exoPlayer?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Can duck — lower volume instead of pausing
                exoPlayer?.volume = currentVolume * 0.2f
            }
        }
    }

    // ── Playback Control ────────────────────────────────────────────

    /**
     * Start playing an ambient audio track from raw resources.
     * Loops infinitely with gapless transitions.
     *
     * @param rawResId Resource ID from `R.raw`, e.g., `R.raw.cabin_noise`
     */
    @OptIn(UnstableApi::class)
    fun play(@RawRes rawResId: Int) {
        // If already playing this exact track, do nothing
        if (currentRawResId == rawResId && exoPlayer?.isPlaying == true) return

        // Request audio focus
        val result = audioManager.requestAudioFocus(audioFocusRequest)
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

        // Release previous player if switching tracks
        release()

        currentRawResId = rawResId

        exoPlayer = ExoPlayer.Builder(context).build().apply {
            // Build URI for raw resource
            val uri: Uri = RawResourceDataSource.buildRawResourceUri(rawResId)
            val mediaItem = MediaItem.fromUri(uri)

            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL   // Gapless infinite loop
            volume = currentVolume
            playWhenReady = true
            prepare()
        }
    }

    /**
     * Pause playback without releasing the player.
     * Call [resume] to continue from the same position.
     */
    fun pause() {
        exoPlayer?.pause()
    }

    /**
     * Resume playback after a pause.
     */
    fun resume() {
        exoPlayer?.play()
    }

    /**
     * Stop playback and release the player resources.
     * Safe to call even if nothing is playing.
     */
    fun stop() {
        release()
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }

    /**
     * Set the playback volume.
     *
     * @param volume Value between 0.0 (silent) and 1.0 (full volume).
     */
    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        exoPlayer?.volume = currentVolume
    }

    /**
     * @return The current volume setting (0.0 – 1.0).
     */
    fun getVolume(): Float = currentVolume

    /**
     * @return `true` if audio is actively playing.
     */
    fun isPlaying(): Boolean = exoPlayer?.isPlaying == true

    /**
     * @return The raw resource ID of the currently loaded track, or null.
     */
    fun getCurrentTrackId(): Int? = currentRawResId

    /**
     * Release the ExoPlayer instance and free memory.
     */
    private fun release() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        currentRawResId = null
    }
}
