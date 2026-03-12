package com.aerofocus.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aerofocus.app.ui.theme.NodeGlow
import com.aerofocus.app.ui.theme.NodeLocked
import com.aerofocus.app.ui.theme.TextSecondary

/**
 * A pulsating glowing dot for the world map, representing a city/destination.
 *
 * - **Unlocked**: Warm amber glow with a pulsating outer ring.
 * - **Locked**: Dim grey dot, no animation.
 *
 * @param isUnlocked Whether this destination has been unlocked.
 * @param size Total bounding size of the composable.
 * @param modifier Modifier.
 */
@Composable
fun GlowingMapNode(
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    if (isUnlocked) {
        // Pulsating glow animation
        val infiniteTransition = rememberInfiniteTransition(label = "nodeGlow")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )

        Canvas(modifier = modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 4f

            // Outer pulsating glow
            drawCircle(
                color = NodeGlow.copy(alpha = pulseAlpha),
                radius = baseRadius * pulseScale * 2f,
                center = center
            )
            // Mid ring
            drawCircle(
                color = NodeGlow.copy(alpha = 0.4f),
                radius = baseRadius * 1.2f,
                center = center
            )
            // Core dot
            drawCircle(
                color = NodeGlow,
                radius = baseRadius,
                center = center
            )
            // Bright center highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = baseRadius * 0.35f,
                center = center
            )
        }
    } else {
        // Locked: static dim node
        Canvas(modifier = modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 4f

            drawCircle(
                color = NodeLocked,
                radius = baseRadius,
                center = center
            )
            drawCircle(
                color = TextSecondary.copy(alpha = 0.3f),
                radius = baseRadius * 0.5f,
                center = center
            )
        }
    }
}
