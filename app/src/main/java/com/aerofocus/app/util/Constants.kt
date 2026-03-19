package com.aerofocus.app.util

/**
 * Application-wide constants for AeroFocus.
 */
object Constants {

    // ── Timer Defaults ───────────────────────────────────────────────
    const val DEFAULT_FOCUS_MINUTES = 25
    const val MIN_FOCUS_MINUTES = 5
    const val MAX_FOCUS_MINUTES = 120

    // ── Gamification ─────────────────────────────────────────────────
    /** 1 completed minute = 1 mile */
    const val MILES_PER_MINUTE = 1

    // ── Notification ─────────────────────────────────────────────────
    const val TIMER_NOTIFICATION_CHANNEL_ID = "aerofocus_timer_channel"
    const val TIMER_NOTIFICATION_ID = 1001

    // ── Service Actions ──────────────────────────────────────────────
    const val ACTION_START = "com.aerofocus.action.START"
    const val ACTION_PAUSE = "com.aerofocus.action.PAUSE"
    const val ACTION_RESUME = "com.aerofocus.action.RESUME"
    const val ACTION_STOP = "com.aerofocus.action.STOP"
    const val ACTION_ABORT = "com.aerofocus.action.ABORT"

    // ── Service Extras ───────────────────────────────────────────────
    const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
    const val EXTRA_DESTINATION_NAME = "extra_destination_name"
    const val EXTRA_FOCUS_TAG = "extra_focus_tag"
    const val EXTRA_STRICT_MODE = "extra_strict_mode"

    // ── Navigation Routes ────────────────────────────────────────────
    const val ROUTE_DEPARTURE = "departure"
    const val ROUTE_PRE_FLIGHT = "pre_flight"
    const val ROUTE_IN_FLIGHT = "in_flight"
    const val ROUTE_ARRIVAL = "arrival/{cityName}/{earnedMiles}/{wasCompleted}"
    const val ROUTE_LOGBOOK = "logbook"

    // ── Audio Tracks ─────────────────────────────────────────────────
    const val AUDIO_CABIN = "cabin_noise"
    const val AUDIO_RAIN = "rain"
    const val AUDIO_FOREST = "forest"

    // ── Focus Tag Presets ────────────────────────────────────────────
    val DEFAULT_FOCUS_TAGS = listOf(
        "Study", "Work", "Reading", "Coding", "Writing",
        "Design", "Research", "Practice", "Planning", "Other"
    )
}
