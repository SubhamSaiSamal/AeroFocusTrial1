package com.aerofocus.app.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aerofocus.app.data.db.entity.FocusSessionEntity
import com.aerofocus.app.ui.theme.DeepNight
import com.aerofocus.app.ui.theme.ErrorRed
import com.aerofocus.app.ui.theme.MutedSunset
import com.aerofocus.app.ui.theme.SuccessGreen
import com.aerofocus.app.ui.theme.SurfaceDark
import com.aerofocus.app.ui.theme.TextPrimary
import com.aerofocus.app.ui.theme.TextSecondary
import com.aerofocus.app.ui.theme.WarmGlow
import com.aerofocus.app.ui.viewmodel.LogbookViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen 5: Flight Logbook.
 *
 * Displays a scrolling list of all past focus sessions styled as
 * stamped passport pages / ticket stubs.
 *
 * Each entry shows:
 * - Status icon (✓ completed / ✗ aborted)
 * - Focus tag and duration
 * - Date/time
 * - Miles earned
 */
@Composable
fun LogbookScreen(
    viewModel: LogbookViewModel = hiltViewModel()
) {
    val sessions by viewModel.allSessions.collectAsState(initial = emptyList())
    val totalMiles by viewModel.totalMiles.collectAsState(initial = 0)
    val completedCount by viewModel.completedFlightCount.collectAsState(initial = 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNight)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // ── Header ──────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = WarmGlow,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Flight Logbook",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Stats Summary ───────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LogbookStat(value = "$totalMiles", label = "Total Miles")
            LogbookStat(value = "$completedCount", label = "Flights")
            LogbookStat(value = "${sessions.size}", label = "Total Logs")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Session List ────────────────────────────────────────
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Flight,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No flights logged yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Book your first flight to start\nyour logbook!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.sessionId }) { session ->
                    FlightLogEntry(session)
                }
            }
        }
    }
}

/**
 * A single flight log entry styled as a ticket stub.
 */
@Composable
private fun FlightLogEntry(session: FocusSessionEntity) {
    val dateFormat = remember {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status icon
        Icon(
            imageVector = if (session.wasCompleted)
                Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = if (session.wasCompleted) "Completed" else "Aborted",
            tint = if (session.wasCompleted) SuccessGreen else ErrorRed,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Session details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.focusTag,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!session.wasCompleted && session.distractingPackage != null) {
                // Parse package name (e.g. "com.instagram.android" -> "Instagram")
                val commonSegments = setOf("com", "org", "net", "android", "google", "apps")
                val appName = session.distractingPackage
                    .split(".")
                    .firstOrNull { it.lowercase() !in commonSegments }
                    ?.replaceFirstChar { it.uppercase() } ?: session.distractingPackage

                // Show the "Black Box" offending app
                Text(
                    text = "Distracted by: $appName",
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "${session.durationMinutes} min • ${dateFormat.format(Date(session.startTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Miles earned
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (session.wasCompleted) "+${session.earnedMiles}" else "0",
                style = MaterialTheme.typography.titleMedium,
                color = if (session.wasCompleted) WarmGlow else TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "miles",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun LogbookStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = WarmGlow,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
