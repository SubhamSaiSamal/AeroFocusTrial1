package com.aerofocus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aerofocus.app.ui.theme.DeepNight
import com.aerofocus.app.ui.theme.FrostedGlass
import com.aerofocus.app.ui.theme.FrostedGlassBorder
import com.aerofocus.app.ui.theme.MutedSunset
import com.aerofocus.app.ui.theme.TextPrimary
import com.aerofocus.app.ui.theme.TextSecondary
import com.aerofocus.app.ui.theme.WarmGlow

/**
 * A frosted-glass styled bottom bar for the In-Flight screen.
 *
 * Contains:
 * - Toggle buttons for ambient audio tracks
 * - A volume slider
 * - An "Emergency Landing" label (the actual long-press is on the parent)
 *
 * The glass effect is achieved via a semi-transparent background with a
 * subtle white border, simulating frosted glass without actual blur
 * (which is expensive and not natively supported in Compose).
 *
 * @param audioTracks List of (trackName, isActive) pairs.
 * @param onTrackToggle Called when a track button is tapped.
 * @param volume Current volume (0..1).
 * @param onVolumeChange Called when the volume slider changes.
 * @param modifier Modifier.
 */
@Composable
fun FrostedGlassBar(
    audioTracks: List<Pair<String, Boolean>>,
    onTrackToggle: (String) -> Unit,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(FrostedGlass)
            .border(
                width = 0.5.dp,
                color = FrostedGlassBorder,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Audio Track Toggles ─────────────────────────────────
        Text(
            text = "Ambient Sound",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            audioTracks.forEach { (name, isActive) ->
                AudioTrackChip(
                    name = name,
                    isActive = isActive,
                    onClick = { onTrackToggle(name) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Volume Slider ───────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Vol",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = WarmGlow,
                    activeTrackColor = WarmGlow,
                    inactiveTrackColor = TextSecondary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

/**
 * Individual audio track toggle chip.
 */
@Composable
private fun AudioTrackChip(
    name: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) WarmGlow.copy(alpha = 0.2f) else DeepNight.copy(alpha = 0.5f)
    val borderColor = if (isActive) WarmGlow else TextSecondary.copy(alpha = 0.3f)
    val textColor = if (isActive) WarmGlow else TextSecondary

    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier
            .height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}
