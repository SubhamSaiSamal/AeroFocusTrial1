package com.aerofocus.app.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

import com.aerofocus.app.service.FlightTimerService
import com.aerofocus.app.service.TimerState
import com.aerofocus.app.ui.components.FrostedGlassBar
import com.aerofocus.app.ui.theme.DeepNight
import com.aerofocus.app.ui.theme.MapBackground
import com.aerofocus.app.ui.theme.MapLandmass
import com.aerofocus.app.ui.theme.MapRouteLine
import com.aerofocus.app.ui.theme.MutedSunset
import com.aerofocus.app.ui.theme.NodeGlow
import com.aerofocus.app.ui.theme.SegmentActive
import com.aerofocus.app.ui.theme.SurfaceDark
import com.aerofocus.app.ui.theme.TextPrimary
import com.aerofocus.app.ui.theme.TextSecondary
import com.aerofocus.app.ui.theme.WarmGlow
import com.aerofocus.app.ui.viewmodel.TimerViewModel
import com.aerofocus.app.util.Constants
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Screen 3: In-Flight — The Active Timer.
 *
 * Features a 3-Way View Toggle:
 * - **View A (Starlit Window)**: Canvas starry sky + timer below
 * - **View B (Flight Map)**: Canvas map with interpolating plane
 * - **View C (Zen Mode)**: Massive glowing timer only
 *
 * Bottom: Frosted-glass bar with audio controls + Emergency Landing.
 */
@Composable
fun InFlightScreen(
    timerViewModel: TimerViewModel,
    onFlightComplete: (cityName: String, earnedMiles: Int, wasCompleted: Boolean) -> Unit
) {
    val timerState by timerViewModel.timerState.collectAsState()
    val remainingMillis by timerViewModel.remainingTimeMillis.collectAsState()
    val destinationName by timerViewModel.destinationName.collectAsState()
    val audioVolume by timerViewModel.audioVolume.collectAsState()
    val activeTrack by timerViewModel.activeTrackName.collectAsState()

    var selectedView by remember { mutableIntStateOf(0) } // 0=Window, 1=Map, 2=Zen
    val progress = timerViewModel.getProgressFraction()
    val formattedTime = FlightTimerService.formatMillisToHHMMSS(remainingMillis)

    // ── CRITICAL: Only navigate on COMPLETED/STOPPED after timer was RUNNING ──
    // The companion FlightTimerService._timerState might still be stale
    // (STOPPED/IDLE from a previous session) when this screen first composes.
    // startForegroundService() is ASYNC — the service intent hasn't been
    // processed yet. If we react to the initial STOPPED value, we'd
    // immediately call onFlightComplete → navigate to Arrival → bounce
    // back to Departure. The fix: track whether we've ever seen RUNNING.
    var hasBeenRunning by remember { mutableStateOf(false) }

    LaunchedEffect(timerState) {
        when (timerState) {
            TimerState.RUNNING -> {
                hasBeenRunning = true
            }
            TimerState.COMPLETED -> {
                if (hasBeenRunning) {
                    val miles = timerViewModel.recordSuccessfulLanding()
                    onFlightComplete(destinationName, miles, true)
                }
            }
            TimerState.STOPPED -> {
                if (hasBeenRunning) {
                    onFlightComplete(destinationName, 0, false)
                }
            }
            else -> { /* IDLE, PAUSED — no navigation */ }
        }
    }

    // Audio track state for the frosted bar
    val audioTracks = listOf(
        Constants.AUDIO_CABIN to (activeTrack == Constants.AUDIO_CABIN),
        Constants.AUDIO_RAIN to (activeTrack == Constants.AUDIO_RAIN),
        Constants.AUDIO_FOREST to (activeTrack == Constants.AUDIO_FOREST)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── 4-Way Segmented Toggle ──────────────────────────
            SegmentedToggle(
                selectedIndex = selectedView,
                onSelect = { selectedView = it },
                items = listOf(
                    ToggleItem(Icons.Default.RemoveRedEye, "Window"),
                    ToggleItem(Icons.Default.Map, "Map"),
                    ToggleItem(Icons.Default.SelfImprovement, "Zen"),
                    ToggleItem(Icons.Default.Warning, "Standby") // Reusing Warning icon as a placeholder for a moon/battery icon
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── View Content ────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = selectedView,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = "viewToggle"
                ) { viewIndex ->
                    when (viewIndex) {
                        0 -> StarlitWindowView(
                            formattedTime = formattedTime,
                            destinationName = destinationName,
                            isPaused = timerState == TimerState.PAUSED
                        )
                        1 -> FlightMapView(
                            progress = progress,
                            destinationName = destinationName,
                            formattedTime = formattedTime
                        )
                        2 -> ZenModeView(
                            formattedTime = formattedTime,
                            isPaused = timerState == TimerState.PAUSED
                        )
                        3 -> StandbyModeView(
                            formattedTime = formattedTime,
                            onWake = { selectedView = 2 } // Wake to Zen mode
                        )
                    }
                }
            }

            // ── Pause/Resume tap area ───────────────────────────
            if (timerState == TimerState.RUNNING || timerState == TimerState.PAUSED) {
                Text(
                    text = if (timerState == TimerState.PAUSED) "TAP TO RESUME" else "TAP TO PAUSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark.copy(alpha = 0.5f))
                        .clickable {
                                if (timerState == TimerState.PAUSED) {
                                    timerViewModel.resumeFlight()
                                } else {
                                    timerViewModel.pauseFlight()
                                }
                        }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Frosted Glass Audio Bar ─────────────────────────
            FrostedGlassBar(
                audioTracks = audioTracks.map { (name, active) ->
                    name.replaceFirstChar { it.uppercase() } to active
                },
                onTrackToggle = { trackName ->
                    val rawName = trackName.lowercase()
                    // We'd need raw resource IDs here — for now, use placeholder logic
                    // The actual R.raw references will be wired when audio assets are added
                    timerViewModel.toggleAudioTrack(0, rawName)
                },
                volume = audioVolume,
                onVolumeChange = { timerViewModel.setAudioVolume(it) }
            )

            // ── Emergency Landing ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MutedSunset.copy(alpha = 0.1f))
                    .clickable { timerViewModel.emergencyLand() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MutedSunset.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LONG PRESS FOR EMERGENCY LANDING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSunset.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Standby Mode View (AMOLED Black, Screen On) ───────────────────

@Composable
fun StandbyModeView(
    formattedTime: String,
    onWake: () -> Unit
) {
    val context = LocalContext.current
    
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onWake() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                color = TextSecondary.copy(alpha = 0.5f) // Very dim for AMOLED burn-in protection
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tap to wake",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary.copy(alpha = 0.3f),
                letterSpacing = 2.sp
            )
        }
    }
}

// ── Segmented Toggle ──────────────────────────────────────────

private data class ToggleItem(val icon: ImageVector, val label: String)

@Composable
private fun SegmentedToggle(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    items: List<ToggleItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) SegmentActive else Color.Transparent)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) WarmGlow else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmGlow
                        )
                    }
                }
            }
        }
    }
}

// ── View A: Starlit Window ──────────────────────────────────────────

@Composable
private fun StarlitWindowView(
    formattedTime: String,
    destinationName: String,
    isPaused: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    // Generate consistent star positions
    val stars = remember {
        val rng = Random(42)
        List(120) {
            StarData(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                size = rng.nextFloat() * 2.5f + 0.5f,
                brightness = rng.nextFloat()
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Starry Window Frame ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(MapBackground),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw stars
                stars.forEach { star ->
                    val alpha = (star.brightness * twinkle).coerceIn(0.1f, 1f)
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = star.size,
                        center = Offset(star.x * size.width, star.y * size.height)
                    )
                }

                // Shooting star (subtle diagonal line)
                val shootX = (twinkle * size.width * 0.6f) + size.width * 0.2f
                val shootY = twinkle * size.height * 0.3f
                if (twinkle > 0.8f) {
                    drawLine(
                        color = Color.White.copy(alpha = (twinkle - 0.8f) * 5f),
                        start = Offset(shootX, shootY),
                        end = Offset(shootX + 40f, shootY + 20f),
                        strokeWidth = 1.5f
                    )
                }

                // Horizon glow at bottom
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            WarmGlow.copy(alpha = 0.05f)
                        ),
                        startY = size.height * 0.7f
                    )
                )
            }

            // Destination label inside window
            Text(
                text = "→ $destinationName",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Timer Display ───────────────────────────────────
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displayLarge,
            color = if (isPaused) WarmGlow.copy(alpha = 0.5f) else WarmGlow,
            textAlign = TextAlign.Center
        )

        if (isPaused) {
            Text(
                text = "FLIGHT PAUSED",
                style = MaterialTheme.typography.labelMedium,
                color = MutedSunset,
                letterSpacing = 2.sp
            )
        }
    }
}

private data class StarData(
    val x: Float,
    val y: Float,
    val size: Float,
    val brightness: Float
)

// ── View B: Flight Map ──────────────────────────────────────────────

@Composable
private fun FlightMapView(
    progress: Float,
    destinationName: String,
    formattedTime: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Map with flight path ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.4f)
                .clip(RoundedCornerShape(24.dp))
                .background(MapBackground)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val w = size.width
                val h = size.height

                // Simplified continent blocks
                val continents = listOf(
                    Offset(w * 0.05f, h * 0.15f) to Offset(w * 0.28f, h * 0.75f),
                    Offset(w * 0.42f, h * 0.1f) to Offset(w * 0.55f, h * 0.7f),
                    Offset(w * 0.55f, h * 0.05f) to Offset(w * 0.88f, h * 0.5f),
                    Offset(w * 0.72f, h * 0.55f) to Offset(w * 0.88f, h * 0.72f),
                )
                continents.forEach { (tl, br) ->
                    drawRoundRect(
                        color = MapLandmass,
                        topLeft = tl,
                        size = androidx.compose.ui.geometry.Size(br.x - tl.x, br.y - tl.y),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                    )
                }

                // Flight route: departure (left) to destination (right)
                val start = Offset(w * 0.15f, h * 0.5f)
                val end = Offset(w * 0.85f, h * 0.35f)
                val controlY = h * 0.1f  // Arc upward

                // Dashed route line (full path)
                drawLine(
                    color = MapRouteLine,
                    start = start,
                    end = end,
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                )

                // Departure node
                drawCircle(color = NodeGlow, radius = 8f, center = start)
                drawCircle(color = NodeGlow.copy(alpha = 0.3f), radius = 16f, center = start)

                // Destination node
                drawCircle(color = NodeGlow, radius = 8f, center = end)
                drawCircle(color = NodeGlow.copy(alpha = 0.3f), radius = 16f, center = end)

                // Plane position interpolated along the route
                val planeX = start.x + (end.x - start.x) * progress
                val planeY = start.y + (end.y - start.y) * progress -
                        (sin(progress * PI.toFloat()) * h * 0.15f)  // Arc

                // Plane trail
                drawLine(
                    color = WarmGlow,
                    start = start,
                    end = Offset(planeX, planeY),
                    strokeWidth = 3f
                )

                // Plane icon (triangle)
                val planeSize = 12f
                val angle = kotlin.math.atan2(
                    end.y - start.y,
                    end.x - start.x
                )
                rotate(
                    degrees = Math.toDegrees(angle.toDouble()).toFloat(),
                    pivot = Offset(planeX, planeY)
                ) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(planeX + planeSize, planeY)
                        lineTo(planeX - planeSize, planeY - planeSize * 0.6f)
                        lineTo(planeX - planeSize * 0.5f, planeY)
                        lineTo(planeX - planeSize, planeY + planeSize * 0.6f)
                        close()
                    }
                    drawPath(path, color = WarmGlow)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress label
        Text(
            text = "${(progress * 100).toInt()}% to $destinationName",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Timer
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displayLarge,
            color = WarmGlow,
            textAlign = TextAlign.Center
        )
    }
}

// ── View C: Zen Mode ────────────────────────────────────────────────

@Composable
private fun ZenModeView(
    formattedTime: String,
    isPaused: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "zenGlow")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zenGlowRadius"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Radial glow behind the timer
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WarmGlow.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.minDimension * glowRadius
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 96.sp
                ),
                color = if (isPaused) WarmGlow.copy(alpha = 0.5f) else WarmGlow,
                textAlign = TextAlign.Center
            )

            if (isPaused) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PAUSED",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedSunset,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}
