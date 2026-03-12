package com.aerofocus.app.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofocus.app.data.db.entity.UnlockedDestinationEntity
import com.aerofocus.app.ui.theme.DeepNight
import com.aerofocus.app.ui.theme.MapBackground
import com.aerofocus.app.ui.theme.MapLandmass
import com.aerofocus.app.ui.theme.MapRouteLine
import com.aerofocus.app.ui.theme.NodeGlow
import com.aerofocus.app.ui.theme.NodeLocked
import com.aerofocus.app.ui.theme.SurfaceDark
import com.aerofocus.app.ui.theme.TextOnAccent
import com.aerofocus.app.ui.theme.TextPrimary
import com.aerofocus.app.ui.theme.TextSecondary
import com.aerofocus.app.ui.theme.WarmGlow
import com.aerofocus.app.ui.viewmodel.FlightViewModel

/**
 * Screen 1: The Departure Lounge (Home).
 *
 * Features:
 * - Minimalist Canvas map with dot-matrix background + smooth continent outlines
 * - Glowing nodes for unlocked destinations, dim dots for locked ones
 * - Dashed route lines connecting unlocked destinations
 * - "Total Miles Flown" prominent header with stats
 * - "Book a Flight" FAB
 */
@Composable
fun DepartureScreen(
    flightViewModel: FlightViewModel,
    onBookFlight: () -> Unit
) {
    val totalMiles by flightViewModel.totalMiles.collectAsState()
    val allDestinations by flightViewModel.allDestinations.collectAsState()
    val completedFlights by flightViewModel.completedFlights.collectAsState()
    val totalFocusMinutes by flightViewModel.totalFocusMinutes.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Header ──────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = WarmGlow,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Departure Lounge",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Total Miles Display ─────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Miles Flown",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$totalMiles",
                    style = MaterialTheme.typography.displayLarge,
                    color = WarmGlow
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "Flights", value = "$completedFlights")
                    StatItem(
                        label = "Focus",
                        value = "${totalFocusMinutes / 60}h ${totalFocusMinutes % 60}m"
                    )
                    StatItem(
                        label = "Unlocked",
                        value = "${allDestinations.count { it.isUnlocked }}/${allDestinations.size}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── World Map Canvas ────────────────────────────────
            Text(
                text = "FLIGHT MAP",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            WorldMapCanvas(
                destinations = allDestinations,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(MapBackground, RoundedCornerShape(24.dp))
            )

            // Bottom spacer for FAB clearance
            Spacer(modifier = Modifier.height(96.dp))
        }

        // ── Book a Flight FAB ───────────────────────────────────
        FloatingActionButton(
            onClick = onBookFlight,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .fillMaxWidth(0.6f)
                .height(56.dp),
            containerColor = WarmGlow,
            contentColor = TextOnAccent,
            shape = RoundedCornerShape(50)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Book a Flight",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Minimalist World Map — Dot Matrix + Smooth Continental Outlines
// ═══════════════════════════════════════════════════════════════════════

/**
 * Canvas-drawn minimalist world map.
 *
 * Instead of blocky rectangles, this uses:
 * 1. A subtle dot-matrix grid across the entire background
 * 2. Smooth [Path] outlines tracing simplified continent shapes
 * 3. Glowing animated nodes for unlocked destinations
 * 4. Dim static dots for locked destinations
 * 5. Dashed route lines connecting sequential unlocked cities
 */
@Composable
private fun WorldMapCanvas(
    destinations: List<UnlockedDestinationEntity>,
    modifier: Modifier = Modifier
) {
    // Glow animation for unlocked nodes
    val infiniteTransition = rememberInfiniteTransition(label = "mapGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Pre-compute continent outlines as normalized coordinate lists
    val continentPaths = remember { buildContinentOutlines() }

    Canvas(modifier = modifier.padding(12.dp)) {
        val w = size.width
        val h = size.height

        // ── Layer 1: Dot Matrix Background ───────────────────────
        // Subtle grid of tiny dots creating a "radar" feel
        val dotSpacing = 18f
        val dotRadius = 0.6f
        val dotColor = MapLandmass.copy(alpha = 0.25f)

        var dx = 0f
        while (dx < w) {
            var dy = 0f
            while (dy < h) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(dx, dy)
                )
                dy += dotSpacing
            }
            dx += dotSpacing
        }

        // ── Layer 2: Continental Outlines (smooth paths) ─────────
        val outlineColor = MapLandmass.copy(alpha = 0.45f)
        val outlineStroke = Stroke(
            width = 1.2f,
            pathEffect = PathEffect.cornerPathEffect(8f)
        )

        continentPaths.forEach { normalizedPoints ->
            if (normalizedPoints.size >= 2) {
                val path = Path().apply {
                    val first = normalizedPoints[0]
                    moveTo(first.first * w, first.second * h)
                    for (i in 1 until normalizedPoints.size) {
                        val pt = normalizedPoints[i]
                        lineTo(pt.first * w, pt.second * h)
                    }
                    close()
                }
                drawPath(path, color = outlineColor, style = outlineStroke)
            }
        }

        // ── Layer 3: Route Lines (dashed) ────────────────────────
        // Mercator-like projection
        fun project(lat: Double, lon: Double): Offset {
            val x = ((lon + 180) / 360.0 * w).toFloat()
            val y = ((90 - lat) / 180.0 * h).toFloat()
            return Offset(x, y)
        }

        val unlocked = destinations.filter { it.isUnlocked }
        for (i in 0 until unlocked.size - 1) {
            val from = project(unlocked[i].latitude, unlocked[i].longitude)
            val to = project(unlocked[i + 1].latitude, unlocked[i + 1].longitude)
            drawLine(
                color = MapRouteLine,
                start = from,
                end = to,
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )
        }

        // ── Layer 4: Destination Nodes ───────────────────────────
        destinations.forEach { dest ->
            val pos = project(dest.latitude, dest.longitude)

            if (dest.isUnlocked) {
                // Outer pulsating glow
                drawCircle(
                    color = NodeGlow.copy(alpha = glowAlpha * 0.2f),
                    radius = 20f,
                    center = pos
                )
                // Mid ring
                drawCircle(
                    color = NodeGlow.copy(alpha = glowAlpha * 0.5f),
                    radius = 10f,
                    center = pos
                )
                // Core
                drawCircle(
                    color = NodeGlow,
                    radius = 5f,
                    center = pos
                )
                // Bright center
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 2f,
                    center = pos
                )
            } else {
                // Locked: dim static dot
                drawCircle(
                    color = NodeLocked.copy(alpha = 0.5f),
                    radius = 3.5f,
                    center = pos
                )
            }
        }
    }
}

/**
 * Builds simplified continent outlines as normalized (0..1) coordinate pairs.
 * These are smooth, minimalist shapes — not cartographically accurate, but
 * recognizable and aesthetically pleasing.
 */
private fun buildContinentOutlines(): List<List<Pair<Float, Float>>> {
    return listOf(
        // ── North America ───────────────────────────────────────
        listOf(
            0.05f to 0.18f, 0.10f to 0.12f, 0.14f to 0.10f,
            0.18f to 0.14f, 0.22f to 0.18f, 0.25f to 0.25f,
            0.23f to 0.32f, 0.20f to 0.38f, 0.18f to 0.42f,
            0.15f to 0.40f, 0.12f to 0.36f, 0.08f to 0.32f,
            0.05f to 0.28f
        ),
        // ── South America ───────────────────────────────────────
        listOf(
            0.18f to 0.48f, 0.21f to 0.50f, 0.23f to 0.55f,
            0.24f to 0.62f, 0.22f to 0.70f, 0.20f to 0.78f,
            0.17f to 0.82f, 0.15f to 0.78f, 0.14f to 0.70f,
            0.15f to 0.60f, 0.16f to 0.52f
        ),
        // ── Europe ──────────────────────────────────────────────
        listOf(
            0.43f to 0.12f, 0.46f to 0.10f, 0.50f to 0.12f,
            0.52f to 0.16f, 0.50f to 0.22f, 0.48f to 0.26f,
            0.45f to 0.28f, 0.42f to 0.24f, 0.41f to 0.18f
        ),
        // ── Africa ──────────────────────────────────────────────
        listOf(
            0.43f to 0.30f, 0.47f to 0.28f, 0.52f to 0.32f,
            0.54f to 0.40f, 0.55f to 0.50f, 0.53f to 0.60f,
            0.50f to 0.68f, 0.47f to 0.72f, 0.44f to 0.68f,
            0.42f to 0.58f, 0.41f to 0.46f, 0.42f to 0.36f
        ),
        // ── Asia ────────────────────────────────────────────────
        listOf(
            0.54f to 0.10f, 0.60f to 0.08f, 0.68f to 0.10f,
            0.75f to 0.15f, 0.80f to 0.20f, 0.82f to 0.28f,
            0.78f to 0.35f, 0.72f to 0.40f, 0.65f to 0.42f,
            0.58f to 0.38f, 0.55f to 0.30f, 0.53f to 0.20f
        ),
        // ── Australia ───────────────────────────────────────────
        listOf(
            0.78f to 0.58f, 0.82f to 0.55f, 0.88f to 0.58f,
            0.90f to 0.65f, 0.87f to 0.72f, 0.82f to 0.74f,
            0.78f to 0.70f, 0.76f to 0.64f
        )
    )
}
