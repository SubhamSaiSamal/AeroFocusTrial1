package com.aerofocus.app.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofocus.app.ui.components.BoardingPassCard
import com.aerofocus.app.ui.components.CircularDurationSlider
import com.aerofocus.app.ui.theme.DeepNight
import com.aerofocus.app.ui.theme.SurfaceDark
import com.aerofocus.app.ui.theme.TextOnAccent
import com.aerofocus.app.ui.theme.TextPrimary
import com.aerofocus.app.ui.theme.TextSecondary
import com.aerofocus.app.ui.theme.WarmGlow
import com.aerofocus.app.ui.viewmodel.FlightViewModel
import com.aerofocus.app.ui.viewmodel.TimerViewModel
import com.aerofocus.app.util.Constants

/**
 * Screen 2: Pre-Flight Ritual (Setup & Boarding Pass).
 *
 * Flow:
 * 1. User sets duration via [CircularDurationSlider]
 * 2. Selects destination from dropdown (unlocked + locked destinations shown)
 * 3. Picks a focus tag from chips
 * 4. Taps "Generate Ticket"
 * 5. BoardingPass overlay appears
 * 6. "Swipe to Tear & Board" starts the timer service → navigates out
 */
@Composable
fun PreFlightScreen(
    flightViewModel: FlightViewModel,
    timerViewModel: TimerViewModel,
    onBoardComplete: () -> Unit,
    onBack: () -> Unit
) {
    val duration by flightViewModel.selectedDuration.collectAsState()
    val selectedDest by flightViewModel.selectedDestination.collectAsState()
    val focusTag by flightViewModel.selectedFocusTag.collectAsState()
    val allDestinations by flightViewModel.allDestinations.collectAsState()
    val departureCity by flightViewModel.departureCity.collectAsState()

    var showBoardingPass by remember { mutableStateOf(false) }
    var showDestDropdown by remember { mutableStateOf(false) }

    // Find the departure destination entity
    val departureDest = allDestinations.find { it.iataCode == departureCity }

    // Selectable destinations: all except the departure city
    val selectableDestinations = allDestinations.filter { it.iataCode != departureCity }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNight)
    ) {
        // ── Main Setup Form ─────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pre-Flight Ritual",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Duration Slider ─────────────────────────────────
            Text(
                text = "SET FLIGHT DURATION",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            CircularDurationSlider(
                currentMinutes = duration,
                onDurationChange = { flightViewModel.setDuration(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Destination Selector ────────────────────────────
            Text(
                text = "SELECT DESTINATION",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .clickable { showDestDropdown = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedDest?.let { "${it.iataCode} — ${it.cityName}" }
                            ?: "Choose your destination",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedDest != null) TextPrimary else TextSecondary
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = TextSecondary
                    )
                }

                DropdownMenu(
                    expanded = showDestDropdown,
                    onDismissRequest = { showDestDropdown = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    selectableDestinations.forEach { dest ->
                        val isUnlocked = dest.isUnlocked
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isUnlocked) Icons.Default.LockOpen
                                        else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isUnlocked) WarmGlow
                                        else TextSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${dest.iataCode} — ${dest.cityName}",
                                            color = if (isUnlocked) TextPrimary
                                            else TextSecondary.copy(alpha = 0.6f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (!isUnlocked) {
                                            Text(
                                                text = "Requires ${dest.requiredMiles} miles",
                                                color = TextSecondary.copy(alpha = 0.4f),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                // Only allow selecting unlocked destinations
                                if (isUnlocked) {
                                    flightViewModel.setDestination(dest)
                                    showDestDropdown = false
                                }
                            },
                            enabled = isUnlocked
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Focus Tag Chips ─────────────────────────────────
            Text(
                text = "WHAT ARE YOU FOCUSING ON?",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Constants.DEFAULT_FOCUS_TAGS.forEach { tag ->
                    val isSelected = tag == focusTag
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) DeepNight else TextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) WarmGlow else SurfaceDark
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) WarmGlow else TextSecondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { flightViewModel.setFocusTag(tag) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Generate Ticket Button ──────────────────────────
            val canGenerate = selectedDest != null && duration > 0
            Button(
                onClick = { showBoardingPass = true },
                enabled = canGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmGlow,
                    contentColor = TextOnAccent,
                    disabledContainerColor = SurfaceDark,
                    disabledContentColor = TextSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (canGenerate) "Generate Ticket" else "Select a Destination",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Boarding Pass Overlay ────────────────────────────────
        AnimatedVisibility(
            visible = showBoardingPass,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut()
        ) {
            selectedDest?.let { dest ->
                BoardingPassCard(
                    departureCode = departureCity,
                    departureName = departureDest?.cityName ?: "Home",
                    destinationCode = dest.iataCode,
                    destinationName = dest.cityName,
                    durationMinutes = duration,
                    focusTag = focusTag,
                    onSwipeToBoardComplete = {
                        // Start the flight!
                        timerViewModel.startFlight(
                            durationMinutes = duration,
                            destinationName = dest.cityName,
                            focusTag = focusTag
                        )
                        onBoardComplete()
                    }
                )
            }
        }
    }
}
