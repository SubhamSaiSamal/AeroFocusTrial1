package com.aerofocus.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofocus.app.ui.theme.DeepNight
import com.aerofocus.app.ui.theme.SurfaceDark
import com.aerofocus.app.ui.theme.TextPrimary
import com.aerofocus.app.ui.theme.TextSecondary
import com.aerofocus.app.ui.theme.WarmGlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Full-screen boarding pass with horizontal "Slide to Board" gesture.
 *
 * When the thumb reaches the far right end (~95%), the callback fires
 * AUTOMATICALLY during the drag — no "release" step needed. A brief
 * tear animation plays (card splits and slides away) before the
 * callback navigates to the next screen.
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

    // ── Slide state ─────────────────────────────────────────────
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    val thumbSizeDp = 54.dp
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val maxDrag = (trackWidthPx - thumbSizePx).coerceAtLeast(1f)
    // slideProgress for UI rendering only (recomputes each frame)
    val slideProgress = (dragOffsetX / maxDrag).coerceIn(0f, 1f)

    // ── Boarding state ──────────────────────────────────────────
    var hasBoarded by remember { mutableStateOf(false) }

    // Tear animation: top half slides up, bottom half slides down
    val topTearOffset by animateFloatAsState(
        targetValue = if (hasBoarded) -600f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "topTear"
    )
    val bottomTearOffset by animateFloatAsState(
        targetValue = if (hasBoarded) 600f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "bottomTear"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (hasBoarded) 0f else 1f,
        animationSpec = tween(350),
        label = "cardAlpha"
    )

    // Fire navigation callback AFTER the tear animation finishes
    LaunchedEffect(hasBoarded) {
        if (hasBoarded) {
            delay(450) // let tear animation play
            onSwipeToBoardComplete()
        }
    }

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
                .alpha(cardAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ═══════════════════════════════════════════════════
            // TOP HALF — ticket info (slides UP on tear)
            // ═══════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, topTearOffset.roundToInt()) }
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(SurfaceDark)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
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

                // Route
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                // Flight Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DetailItem(label = "DURATION", value = "${durationMinutes}m")
                    DetailItem(label = "CLASS", value = "FOCUS")
                    DetailItem(label = "TAG", value = focusTag.uppercase().take(8))
                }
            }

            // Perforated tear line
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            ) {
                drawLine(
                    color = WarmGlow.copy(alpha = 0.4f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(6f, 4f), 0f
                    ),
                    strokeWidth = 2f
                )
            }

            // ═══════════════════════════════════════════════════
            // BOTTOM HALF — slider (slides DOWN on tear)
            // ═══════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, bottomTearOffset.roundToInt()) }
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Slide track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(thumbSizeDp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    WarmGlow.copy(alpha = 0.08f),
                                    WarmGlow.copy(alpha = 0.08f + slideProgress * 0.25f)
                                )
                            )
                        )
                        .onSizeChanged { trackWidthPx = it.width.toFloat() }
                ) {
                    // Chevron hints (fade as you drag)
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha((1f - slideProgress * 2f).coerceIn(0f, 0.5f))
                            .padding(start = thumbSizeDp + 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(3) { i ->
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = WarmGlow.copy(alpha = 0.3f - i * 0.08f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SLIDE TO BOARD",
                            style = MaterialTheme.typography.labelMedium,
                            color = WarmGlow.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )
                    }

                    // Progress fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(slideProgress.coerceAtLeast(0.01f))
                            .height(thumbSizeDp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        WarmGlow.copy(alpha = 0.15f),
                                        WarmGlow.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )

                    // ★ DRAGGABLE THUMB ★
                    // Auto-triggers at 95% DURING the drag — no release needed
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(dragOffsetX.roundToInt(), 0) }
                            .size(thumbSizeDp)
                            .clip(CircleShape)
                            .background(
                                if (hasBoarded) WarmGlow
                                else WarmGlow.copy(alpha = 0.85f + slideProgress * 0.15f)
                            )
                            .pointerInput(Unit) {
                                if (hasBoarded) return@pointerInput
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        // If somehow we get here without triggering,
                                        // check and fire. Otherwise snap back.
                                        val currentMax = (trackWidthPx - thumbSizePx)
                                            .coerceAtLeast(1f)
                                        val currentProgress = dragOffsetX / currentMax
                                        if (currentProgress >= 0.90f && !hasBoarded) {
                                            hasBoarded = true
                                        } else if (!hasBoarded) {
                                            scope.launch {
                                                val anim = Animatable(dragOffsetX)
                                                anim.animateTo(
                                                    0f,
                                                    tween(300, easing = FastOutSlowInEasing)
                                                ) { dragOffsetX = value }
                                            }
                                        }
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        if (hasBoarded) return@detectHorizontalDragGestures
                                        change.consume()
                                        val currentMax = (trackWidthPx - thumbSizePx)
                                            .coerceAtLeast(1f)
                                        dragOffsetX = (dragOffsetX + dragAmount)
                                            .coerceIn(0f, currentMax)

                                        // ★ AUTO-TRIGGER at 95% — no release needed ★
                                        val currentProgress = dragOffsetX / currentMax
                                        if (currentProgress >= 0.95f && !hasBoarded) {
                                            hasBoarded = true
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = "Board",
                            tint = DeepNight,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(1f + slideProgress * 0.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        hasBoarded -> "✈️ BOARDING COMPLETE"
                        slideProgress > 0.1f -> "${(slideProgress * 100).toInt()}%"
                        else -> "Drag the plane to board your flight"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasBoarded) WarmGlow else TextSecondary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
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
