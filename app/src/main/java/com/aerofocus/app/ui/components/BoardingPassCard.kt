package com.aerofocus.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofocus.app.ui.theme.DeepNight
import com.aerofocus.app.ui.theme.MutedSunset
import com.aerofocus.app.ui.theme.SurfaceDark
import com.aerofocus.app.ui.theme.TextPrimary
import com.aerofocus.app.ui.theme.TextSecondary
import com.aerofocus.app.ui.theme.WarmGlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Full-screen boarding pass card with aviation-themed design.
 *
 * Features:
 * - Header with flight icon and airline branding
 * - Departure ↔ Destination with IATA codes
 * - Duration, focus tag, and date
 * - Dashed "tear here" perforated line
 * - **Swipe down to tear & board** gesture that triggers the timer start
 *
 * @param departureCode IATA code of departure city.
 * @param departureName Full name of departure city.
 * @param destinationCode IATA code of destination.
 * @param destinationName Full name of destination.
 * @param durationMinutes Selected focus duration.
 * @param focusTag What the user is focusing on.
 * @param onSwipeToBoardComplete Called when the swipe gesture completes.
 * @param modifier Modifier.
 */
@Composable
fun BoardingPassCard(
    departureCode: String,
    departureName: String,
    destinationCode: String,
    destinationName: String,
    durationMinutes: Int,
    focusTag: String,
    onSwipeToBoardComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val tearThreshold = with(density) { 200.dp.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val tearProgress = (dragOffset / tearThreshold).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNight.copy(alpha = 0.95f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, dragOffset.roundToInt()) }
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header ──────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Flight,
                    contentDescription = null,
                    tint = WarmGlow,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AEROFOCUS AIRLINES",
                    style = MaterialTheme.typography.labelLarge,
                    color = WarmGlow,
                    letterSpacing = 3.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Route: DEP → DEST ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Departure
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = departureCode,
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = departureName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Plane path
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AirplanemodeActive,
                        contentDescription = null,
                        tint = WarmGlow,
                        modifier = Modifier.size(24.dp)
                    )
                    // Dashed flight path line
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        drawLine(
                            color = TextSecondary,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(8f, 6f), 0f
                            ),
                            strokeWidth = 2f
                        )
                    }
                }

                // Destination
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = destinationCode,
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = destinationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Flight Details ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailItem(label = "DURATION", value = "${durationMinutes}m")
                DetailItem(label = "CLASS", value = "FOCUS")
                DetailItem(label = "TAG", value = focusTag.uppercase().take(8))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Perforated tear line ────────────────────────────
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            ) {
                drawLine(
                    color = TextSecondary.copy(alpha = 0.5f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(4f, 4f), 0f
                    ),
                    strokeWidth = 2f
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Swipe to Board ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        WarmGlow.copy(
                            alpha = 0.15f + (tearProgress * 0.85f)
                        )
                    )
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (tearProgress >= 1f) {
                                    onSwipeToBoardComplete()
                                } else {
                                    // Snap back
                                    scope.launch {
                                        dragOffset = 0f
                                    }
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        tint = if (tearProgress > 0.5f) DeepNight else WarmGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (tearProgress >= 1f) "BOARDING!" else "↓ SWIPE DOWN TO BOARD",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (tearProgress > 0.5f) DeepNight else WarmGlow,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}
