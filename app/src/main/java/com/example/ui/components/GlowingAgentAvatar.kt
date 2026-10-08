package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AgentState
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed

@Composable
fun GlowingAgentAvatar(
    agentState: AgentState,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    val stateColor by animateColorAsState(
        targetValue = when (agentState) {
            AgentState.IDLE -> NeonCyan
            AgentState.THINKING -> NeonAmber
            AgentState.EXECUTING_TOOL -> NeonPurple
            AgentState.SYNTHESIZING -> NeonCyan
            AgentState.COMPLETE -> NeonGreen
            AgentState.ERROR -> NeonRed
        },
        animationSpec = tween(500),
        label = "avatarStateColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (agentState == AgentState.EXECUTING_TOOL) 700 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing energy ring
        Canvas(
            modifier = Modifier
                .size(size)
                .scale(pulseScale)
        ) {
            drawCircle(
                color = stateColor.copy(alpha = 0.25f),
                radius = this.size.minDimension / 2,
                style = Stroke(width = 4.dp.toPx())
            )
        }

        // Mid rotating cybernetic arc
        Canvas(
            modifier = Modifier.size(size * 0.85f)
        ) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        stateColor.copy(alpha = 0.1f),
                        stateColor,
                        stateColor.copy(alpha = 0.1f)
                    )
                ),
                startAngle = ringRotation,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Inner glowing core
        Box(
            modifier = Modifier
                .size(size * 0.65f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            stateColor.copy(alpha = 0.35f),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(2.dp, stateColor.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (agentState) {
                AgentState.IDLE -> Icons.Default.Psychology
                AgentState.THINKING -> Icons.Default.AutoAwesome
                AgentState.EXECUTING_TOOL -> Icons.Default.Terminal
                AgentState.SYNTHESIZING -> Icons.Default.Memory
                AgentState.COMPLETE -> Icons.Default.AutoAwesome
                AgentState.ERROR -> Icons.Default.Psychology
            }

            Icon(
                imageVector = icon,
                contentDescription = "Agent Core",
                tint = stateColor,
                modifier = Modifier.size(size * 0.35f)
            )
        }
    }
}
