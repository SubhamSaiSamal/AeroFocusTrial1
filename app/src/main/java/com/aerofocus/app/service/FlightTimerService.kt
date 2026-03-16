package com.aerofocus.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.aerofocus.app.MainActivity
import com.aerofocus.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The heart of AeroFocus — a foreground service that keeps the timer
 * alive regardless of Doze mode, screen state, or app navigation.
 *
 * ## How it works
 * 1. Started via explicit [Intent] with an action constant from [Constants].
 * 2. Immediately promotes itself to foreground with an ongoing notification.
 * 3. Runs a coroutine-based 1-second ticker, emitting remaining time via [remainingTimeFlow].
 * 4. UI observes the companion [StateFlow]s reactively.
 * 5. On completion or cancellation, stops itself and cleans up.
 *
 * ## Notification
 * Persistent, non-dismissible, shows destination + countdown + pause/stop actions.
 */
@AndroidEntryPoint
class FlightTimerService : Service() {

    // ── Service-scoped coroutine management ──────────────────────────
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickerJob: Job? = null

    // ── Flight metadata (set on START) ──────────────────────────────
    private var destinationName: String = ""
    private var focusTag: String = ""
    private var totalDurationMillis: Long = 0L

    private lateinit var notificationManager: NotificationManager
    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        setupMediaSession()
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "AeroFocusMediaSession").apply {
            isActive = true
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 1.0f)
                    .build()
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START -> handleStart(intent)
            Constants.ACTION_PAUSE -> handlePause()
            Constants.ACTION_RESUME -> handleResume()
            Constants.ACTION_STOP -> handleStop()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        tickerJob?.cancel()
        serviceScope.cancel()
        // Reset companion state
        _timerState.value = TimerState.IDLE
        _remainingTimeMillis.value = 0L
        _totalDurationMillis.value = 0L
        _destinationName.value = ""
        stopService(Intent(this, FocusEnforcerService::class.java))
        
        mediaSession?.isActive = false
        mediaSession?.release()
        
        super.onDestroy()
    }

    // ── Action Handlers ─────────────────────────────────────────────

    private fun handleStart(intent: Intent) {
        val durationMinutes = intent.getIntExtra(Constants.EXTRA_DURATION_MINUTES, 25)
        destinationName = intent.getStringExtra(Constants.EXTRA_DESTINATION_NAME) ?: "Unknown"
        focusTag = intent.getStringExtra(Constants.EXTRA_FOCUS_TAG) ?: ""
        val isStrictMode = intent.getBooleanExtra(Constants.EXTRA_STRICT_MODE, false)
        totalDurationMillis = durationMinutes * 60 * 1000L

        // Publish to companion state
        _totalDurationMillis.value = totalDurationMillis
        _remainingTimeMillis.value = totalDurationMillis
        _destinationName.value = destinationName
        _timerState.value = TimerState.RUNNING

        // Start the App Blocking Enforcer conditionally
        if (isStrictMode) {
            startService(Intent(this, FocusEnforcerService::class.java))
        }

        // Foreground promotion — must happen within 5 seconds of startForegroundService()
        startForeground(Constants.TIMER_NOTIFICATION_ID, buildNotification())

        startTicker()
    }

    private fun handlePause() {
        tickerJob?.cancel()
        _timerState.value = TimerState.PAUSED
        updateNotification()
    }

    private fun handleResume() {
        _timerState.value = TimerState.RUNNING
        startTicker()
        updateNotification()
    }

    private fun handleStop() {
        tickerJob?.cancel()
        _timerState.value = TimerState.STOPPED
        stopService(Intent(this, FocusEnforcerService::class.java))
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ── Ticker Logic ────────────────────────────────────────────────

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (_remainingTimeMillis.value > 0L) {
                delay(1000L)
                val newValue = _remainingTimeMillis.value - 1000L
                _remainingTimeMillis.value = maxOf(newValue, 0L)
                updateNotification()
            }
            // Timer reached zero — successful landing
            _timerState.value = TimerState.COMPLETED
            updateNotification()
            // Service stays alive briefly for the UI to read COMPLETED state,
            // then the ViewModel will stop it after recording the session.
        }
    }

    // ── Notification ────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.TIMER_NOTIFICATION_CHANNEL_ID,
            "Flight Timer",
            NotificationManager.IMPORTANCE_LOW   // No sound, persistent
        ).apply {
            description = "Shows the active focus flight countdown"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val remaining = _remainingTimeMillis.value
        val timeText = formatMillisToMMSS(remaining)
        val stateLabel = when (_timerState.value) {
            TimerState.PAUSED -> "⏸ PAUSED"
            TimerState.COMPLETED -> "✈️ LANDED"
            else -> "✈️ En Route"
        }

        // Tap notification → open app
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, Constants.TIMER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("$stateLabel → $destinationName")
            .setContentText("$timeText remaining")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Pause / Resume action
        if (_timerState.value == TimerState.RUNNING) {
            val pauseIntent = PendingIntent.getService(
                this, 1,
                Intent(this, FlightTimerService::class.java).apply {
                    action = Constants.ACTION_PAUSE
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(
                android.R.drawable.ic_media_pause,
                "Pause",
                pauseIntent
            )
        } else if (_timerState.value == TimerState.PAUSED) {
            val resumeIntent = PendingIntent.getService(
                this, 2,
                Intent(this, FlightTimerService::class.java).apply {
                    action = Constants.ACTION_RESUME
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(
                android.R.drawable.ic_media_play,
                "Resume",
                resumeIntent
            )
        }

        // Stop action (always available unless completed)
        if (_timerState.value != TimerState.COMPLETED) {
            val stopIntent = PendingIntent.getService(
                this, 3,
                Intent(this, FlightTimerService::class.java).apply {
                    action = Constants.ACTION_STOP
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(
                android.R.drawable.ic_delete,
                "Emergency Land",
                stopIntent
            )
        }

        // Progress bar
        if (_timerState.value == TimerState.RUNNING || _timerState.value == TimerState.PAUSED) {
            val progress = ((totalDurationMillis - remaining) * 100 / totalDurationMillis).toInt()
            builder.setProgress(100, progress, false)
        }

        builder.setStyle(
            MediaNotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1) // Show Pause/Resume and Stop in compact view
                .setMediaSession(mediaSession?.sessionToken)
        )

        return builder.build()
    }

    private fun updateNotification() {
        try {
            notificationManager.notify(Constants.TIMER_NOTIFICATION_ID, buildNotification())
        } catch (_: Exception) {
            // Notification permission may not be granted — timer still runs
        }
    }

    // ── Companion: Shared State (observed by UI) ────────────────────

    companion object {

        private val _timerState = MutableStateFlow(TimerState.IDLE)
        val timerStateFlow: StateFlow<TimerState> = _timerState.asStateFlow()

        private val _remainingTimeMillis = MutableStateFlow(0L)
        val remainingTimeFlow: StateFlow<Long> = _remainingTimeMillis.asStateFlow()

        private val _totalDurationMillis = MutableStateFlow(0L)
        val totalDurationFlow: StateFlow<Long> = _totalDurationMillis.asStateFlow()

        private val _destinationName = MutableStateFlow("")
        val destinationNameFlow: StateFlow<String> = _destinationName.asStateFlow()

        /**
         * Convenience: the elapsed fraction (0.0 → 1.0) for progress visuals.
         */
        fun getProgressFraction(): Float {
            val total = _totalDurationMillis.value
            if (total <= 0) return 0f
            val remaining = _remainingTimeMillis.value
            return ((total - remaining).toFloat() / total).coerceIn(0f, 1f)
        }

        /** Formats millis to "MM:SS" string for display. */
        fun formatMillisToMMSS(millis: Long): String {
            val totalSeconds = millis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

        /** Formats millis to "HH:MM:SS" for sessions over 60 minutes. */
        fun formatMillisToHHMMSS(millis: Long): String {
            val totalSeconds = millis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%02d:%02d".format(minutes, seconds)
            }
        }
    }
}

/**
 * Represents the lifecycle states of a focus flight.
 */
enum class TimerState {
    /** No active session. */
    IDLE,
    /** Countdown actively ticking. */
    RUNNING,
    /** User pressed pause — timer frozen. */
    PAUSED,
    /** Timer reached zero — successful landing. */
    COMPLETED,
    /** User triggered emergency landing or service was killed. */
    STOPPED
}
