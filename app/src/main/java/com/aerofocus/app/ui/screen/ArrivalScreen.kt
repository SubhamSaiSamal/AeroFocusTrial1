package com.aerofocus.app.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.media.RingtoneManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import com.aerofocus.app.ui.theme.DeepNight
import com.aerofocus.app.ui.theme.ErrorRed
import com.aerofocus.app.ui.theme.MutedSunset
import com.aerofocus.app.ui.theme.SuccessGreen
import com.aerofocus.app.ui.theme.SurfaceDark
import com.aerofocus.app.ui.theme.TextOnAccent
import com.aerofocus.app.ui.theme.TextPrimary
import com.aerofocus.app.ui.theme.TextSecondary
import com.aerofocus.app.ui.theme.WarmGlow

/**
 * Screen 4: Arrival — Post-flight summary.
 *
 * Displays:
 * - Success: "Touchdown in [City]! You earned X miles."
 * - Failure: "Emergency Landing — no miles earned."
 *
 * Beautiful animated card with celebration particles on success.
 */
@Composable
fun ArrivalScreen(
    cityName: String,
    earnedMiles: Int,
    wasCompleted: Boolean,
    onReturnToLounge: () -> Unit
) {
    val context = LocalContext.current
    
    // Entrance animation
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(
            1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }

    // Layover Timer State
    var layoverRemainingSeconds by remember { mutableIntStateOf(0) }
    var isLayoverActive by remember { mutableStateOf(false) }

    LaunchedEffect(isLayoverActive, layoverRemainingSeconds) {
        if (isLayoverActive && layoverRemainingSeconds > 0) {
            delay(1000L)
            layoverRemainingSeconds -= 1
            if (layoverRemainingSeconds == 0) {
                isLayoverActive = false
                // Play system notification sound when layover ends
                try {
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context, uri)
                    ringtone.play()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNight)
    ) {
        // Celebration particles (only on success)
        if (wasCompleted) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val particleCount = 30
                val rng = kotlin.random.Random(System.currentTimeMillis() / 1000)
                repeat(particleCount) { i ->
                    val x = rng.nextFloat() * size.width
                    val y = rng.nextFloat() * size.height * animProgress.value
                    val particleSize = rng.nextFloat() * 4f + 1f
                    val colors = listOf(WarmGlow, MutedSunset, SuccessGreen, Color.White)
                    drawCircle(
                        color = colors[i % colors.size].copy(
                            alpha = (1f - y / size.height) * 0.6f
                        ),
                        radius = particleSize,
                        center = Offset(x, y)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Icon ────────────────────────────────────────────
            Icon(
                imageVector = if (wasCompleted) Icons.Default.FlightLand
                else Icons.Default.Warning,
                contentDescription = null,
                tint = if (wasCompleted) WarmGlow else MutedSunset,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Title ───────────────────────────────────────────
            Text(
                text = if (wasCompleted) "Touchdown!" else "Emergency Landing",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Subtitle ────────────────────────────────────────
            Text(
                text = if (wasCompleted)
                    "You've arrived in $cityName"
                else
                    "Flight to $cityName was aborted",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Summary Card ────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (wasCompleted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FLIGHT COMPLETE",
                            style = MaterialTheme.typography.labelMedium,
                            color = SuccessGreen,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "+$earnedMiles",
                        style = MaterialTheme.typography.displayLarge,
                        color = WarmGlow
                    )
                    Text(
                        text = "miles earned",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    Text(
                        text = "No miles earned",
                        style = MaterialTheme.typography.titleLarge,
                        color = MutedSunset
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Complete your next flight to earn miles\nand unlock new destinations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Layover Options (Only on Success) ───────────────
            if (wasCompleted) {
                Spacer(modifier = Modifier.height(32.dp))
                
                if (isLayoverActive) {
                    // Active Layover Timer UI
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LAYOVER ACTIVE",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%02d:%02d", layoverRemainingSeconds / 60, layoverRemainingSeconds % 60),
                            style = MaterialTheme.typography.displayLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { isLayoverActive = false; layoverRemainingSeconds = 0 },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }
                    }
                } else {
                    // Start Layover Choices
                    Text(
                        text = "START LAYOVER",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { layoverRemainingSeconds = 5 * 60; isLayoverActive = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                        ) {
                            Text("5 Min Break", color = TextPrimary)
                        }
                        Button(
                            onClick = { layoverRemainingSeconds = 10 * 60; isLayoverActive = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                        ) {
                            Text("10 Min Break", color = TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Return Button ───────────────────────────────────
            Button(
                onClick = onReturnToLounge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmGlow,
                    contentColor = TextOnAccent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Return to Departure Lounge",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
