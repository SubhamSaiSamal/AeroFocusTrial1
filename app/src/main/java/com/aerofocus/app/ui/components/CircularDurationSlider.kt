package com.aerofocus.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aerofocus.app.ui.theme.DeepNight
import com.aerofocus.app.ui.theme.SurfaceDark
import com.aerofocus.app.ui.theme.TextSecondary
import com.aerofocus.app.ui.theme.WarmGlow
import com.aerofocus.app.util.Constants
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * A circular arc slider for selecting focus duration.
 *
 * The user drags a thumb around a 270° arc to set the timer duration
 * between [Constants.MIN_FOCUS_MINUTES] and [Constants.MAX_FOCUS_MINUTES].
 *
 * The center displays the selected duration in large serif type.
 *
 * @param currentMinutes The currently selected duration.
 * @param onDurationChange Called when the user drags the thumb to a new value.
 * @param modifier Modifier for the composable.
 * @param size Diameter of the circular slider.
 */
@Composable
fun CircularDurationSlider(
    currentMinutes: Int,
    onDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp
) {
    val minMinutes = Constants.MIN_FOCUS_MINUTES
    val maxMinutes = Constants.MAX_FOCUS_MINUTES

    // Angle calculation: arc spans 270° starting from 135° (bottom-left)
    val startAngleDeg = 135f
    val sweepAngleDeg = 270f

    // Current value as a fraction 0..1
    val fraction = ((currentMinutes - minMinutes).toFloat() / (maxMinutes - minMinutes))
        .coerceIn(0f, 1f)

    var dragAngle by remember { mutableFloatStateOf(fraction * sweepAngleDeg) }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val center = Offset(
                            this.size.width / 2f,
                            this.size.height / 2f
                        )
                        val touch = change.position
                        // Calculate angle from center
                        var angle = Math
                            .toDegrees(
                                atan2(
                                    (touch.y - center.y).toDouble(),
                                    (touch.x - center.x).toDouble()
                                )
                            )
                            .toFloat()
                        // Normalize to 0..360
                        if (angle < 0) angle += 360f

                        // Map angle to slider range (135° to 45° clockwise = 270°)
                        var relative = angle - startAngleDeg
                        if (relative < 0) relative += 360f
                        if (relative > sweepAngleDeg) {
                            // Outside the arc — snap to nearest end
                            relative = if (relative > sweepAngleDeg + 45f) 0f else sweepAngleDeg
                        }

                        dragAngle = relative
                        val newFraction = (relative / sweepAngleDeg).coerceIn(0f, 1f)
                        val newMinutes = (minMinutes + newFraction * (maxMinutes - minMinutes))
                            .roundToInt()
                            .coerceIn(minMinutes, maxMinutes)
                        // Snap to 5-minute increments
                        val snapped = ((newMinutes + 2) / 5) * 5
                        onDurationChange(snapped.coerceIn(minMinutes, maxMinutes))
                    }
                }
        ) {
            val strokeWidth = 14.dp.toPx()
            val thumbRadius = 16.dp.toPx()
            val padding = thumbRadius + 4.dp.toPx()
            val arcSize = this.size.width - padding * 2

            // ── Background track ────────────────────────────────
            drawArc(
                color = SurfaceDark,
                startAngle = startAngleDeg,
                sweepAngle = sweepAngleDeg,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // ── Active arc (gradient feel) ──────────────────────
            val activeSweep = fraction * sweepAngleDeg
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        WarmGlow.copy(alpha = 0.4f),
                        WarmGlow
                    )
                ),
                startAngle = startAngleDeg,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // ── Thumb ───────────────────────────────────────────
            val thumbAngleRad = Math.toRadians(
                (startAngleDeg + activeSweep).toDouble()
            ).toFloat()
            val arcRadius = arcSize / 2f
            val centerX = this.size.width / 2f
            val centerY = this.size.height / 2f
            val thumbX = centerX + arcRadius * cos(thumbAngleRad)
            val thumbY = centerY + arcRadius * sin(thumbAngleRad)

            // Outer glow
            drawCircle(
                color = WarmGlow.copy(alpha = 0.3f),
                radius = thumbRadius * 1.5f,
                center = Offset(thumbX, thumbY)
            )
            // Thumb circle
            drawCircle(
                color = WarmGlow,
                radius = thumbRadius,
                center = Offset(thumbX, thumbY)
            )
            // Inner dot
            drawCircle(
                color = DeepNight,
                radius = thumbRadius * 0.4f,
                center = Offset(thumbX, thumbY)
            )
        }

        // ── Center label ────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$currentMinutes",
                style = MaterialTheme.typography.displayLarge,
                color = WarmGlow
            )
            Text(
                text = "minutes",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
