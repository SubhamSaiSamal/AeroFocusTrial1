package com.aerofocus.app.service

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.aerofocus.app.MainActivity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.aerofocus.app.data.repository.AeroFocusRepository
import com.aerofocus.app.data.db.entity.FocusSessionEntity
import com.aerofocus.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Background enforcer that monitors foreground apps using UsageStatsManager.
 * If a blocked package is detected while the timer is active, it ruthlessly
 * redirects the user back to the AeroFocus MainActivity.
 */
@AndroidEntryPoint
class FocusEnforcerService : Service() {

    @Inject
    lateinit var repository: AeroFocusRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var enforcerJob: Job? = null
    private lateinit var usageStatsManager: UsageStatsManager

    // Predefined list of highly distracting apps to block during focus time
    private val blockedPackages = setOf(
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music",
        "com.instagram.android",
        "com.facebook.katana",
        "com.twitter.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.reddit.frontpage",
        "tv.twitch.android.app"
    )

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startEnforcer()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        enforcerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startEnforcer() {
        enforcerJob?.cancel()
        enforcerJob = serviceScope.launch {
            while (true) {
                // Only enforce if the timer is actually RUNNING
                if (FlightTimerService.timerStateFlow.value == TimerState.RUNNING) {
                    val foregroundApp = getForegroundPackage()
                    if (foregroundApp != null && blockedPackages.contains(foregroundApp)) {
                        punishUser()
                    }
                }
                delay(1000L) // Check every second
            }
        }
    }

    private fun getForegroundPackage(): String? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 2000 // Look back 2 seconds

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var currentForegroundPackage: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentForegroundPackage = event.packageName
            }
        }
        return currentForegroundPackage
    }

    private fun punishUser() {
        // 1. Log the "Walk of Shame"
        val flightDuration = FlightTimerService.totalDurationFlow.value / (60 * 1000L)
        serviceScope.launch {
            repository.insertSession(
                FocusSessionEntity(
                    startTime = System.currentTimeMillis() - (FlightTimerService.totalDurationFlow.value - FlightTimerService.remainingTimeFlow.value),
                    durationMinutes = flightDuration.toInt(),
                    focusTag = "ABORTED: Distraction",
                    wasCompleted = false,
                    earnedMiles = 0
                )
            )
        }

        // 2. Explicitly stop the timer (kills the notification)
        val stopIntent = Intent(this, FlightTimerService::class.java).apply {
            action = Constants.ACTION_STOP
        }
        startService(stopIntent)
        
        // 2.5 Ensure the old notification is dead
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.TIMER_NOTIFICATION_ID)

        // 3. Fire High-Priority Heads-Up Notification
        val channelId = "aerofocus_abort_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Flight Abort Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts when a flight is forcefully aborted"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val abortNotification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 FLIGHT ABORTED")
            .setContentText("Distracting app detected. You have been grounded.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(1002, abortNotification)

        // 4. Trigger harsh Haptics
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (vibrator.hasVibrator()) {
            val pattern = longArrayOf(0, 200, 100, 200, 100, 500) // SOS-like stutter
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }

        // 5. Fire Screen Toast (needs main thread)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, "🚨 FLIGHT ABORTED! Distraction detected.", Toast.LENGTH_LONG).show()
        }

        // 6. Drag the user back to the app
        val returnIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(returnIntent)
    }
}
