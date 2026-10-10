package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var bootStepIndex by remember { mutableIntStateOf(0) }
    var isReady by remember { mutableStateOf(false) }

    val bootLogSteps = remember {
        listOf(
            "INITIALIZING NEURAL SYSTEM CORE...",
            "AUTHENTICATING DEVELOPER // C9-SHANICE Exploiter",
            "SYNCING ANYLINK REMOTE ENGINE & P2P SOCKETS...",
            "LOADING WHATSAPP ASSISTANT & AUTO-REPLY PIPELINE...",
            "STARTING C9-SHANICE AI AGENT // SYSTEM ONLINE"
        )
    }

    // Infinite transitions for cyber visual effects
    val infiniteTransition = rememberInfiniteTransition(label = "splash_infinite")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rotation"
    )

    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Animated boot progress
    LaunchedEffect(Unit) {
        val totalSteps = 100
        for (i in 1..totalSteps) {
            delay(28) // ~2.8 seconds total
            progress = i / 100f

            when {
                i < 20 -> bootStepIndex = 0
                i < 45 -> bootStepIndex = 1
                i < 70 -> bootStepIndex = 2
                i < 90 -> bootStepIndex = 3
                else -> bootStepIndex = 4
            }
        }
        isReady = true
        delay(400)
        onFinish()
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100),
        label = "progress_anim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("splash_screen_container")
    ) {
        // Background Grid and Scanning Light Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Background cyber grid lines
            val gridSpacing = 40.dp.toPx()
            val gridColor = Color(0xFF00F0FF).copy(alpha = 0.04f)

            var x = 0f
            while (x <= canvasWidth) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }

            var y = 0f
            while (y <= canvasHeight) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }

            // Radial cyber glow at center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.15f),
                        NeonPurple.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(canvasWidth / 2f, canvasHeight / 2.6f),
                    radius = canvasWidth * 0.75f
                ),
                radius = canvasWidth * 0.75f,
                center = Offset(canvasWidth / 2f, canvasHeight / 2.6f)
            )
        }

        // Top skip action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isReady) NeonGreen else NeonCyan)
                )
                Text(
                    text = if (isReady) "SYSTEM ONLINE" else "SYSTEM BOOT // V2.5",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isReady) NeonGreen else NeonCyan
                )
            }

            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberSurface.copy(alpha = 0.7f),
                    contentColor = TextSecondary
                ),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("splash_skip_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("SKIP", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Skip Splash",
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Center Content: Glowing Emblem, App Name, Developer Badge
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Animated Cyber Insignia Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(170.dp)
                    .scale(pulseScale)
            ) {
                // Outer rotating segmented tech ring
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotationAngle)
                ) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                NeonCyan,
                                Color.Transparent,
                                NeonPurple,
                                Color.Transparent,
                                NeonCyan
                            )
                        ),
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }

                // Counter-rotating tech orbit
                Canvas(
                    modifier = Modifier
                        .size(135.dp)
                        .rotate(counterRotationAngle)
                ) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                NeonPurple.copy(alpha = 0.8f),
                                Color.Transparent,
                                NeonGreen.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        ),
                        style = Stroke(
                            width = 1.5.dp.toPx()
                        )
                    )
                }

                // Inner glowing sphere
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(105.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyberSurface,
                                    Color(0xFF070B14)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(NeonCyan.copy(alpha = glowAlpha), NeonPurple.copy(alpha = glowAlpha))
                            ),
                            shape = CircleShape
                        )
                ) {
                    // Center Icon / Hologram
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "C9-SHANICE AI Agent Emblem",
                        tint = NeonCyan,
                        modifier = Modifier
                            .size(54.dp)
                            .alpha(glowAlpha)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // APP NAME (As requested by user: C9-SHANICE AI Agent)
            Text(
                text = "C9-SHANICE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            // Animated Gradient Title Subtext
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeonCyan, NeonPurple)
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "AI AGENT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.5.sp,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "V2.5 HYBRID",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            Text(
                text = "AUTONOMOUS MOBILE CORE & ANYLINK REMOTE MATRIX",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // DEVELOPER BRANDING BADGE (As requested: Developer Name C9-SHANICE Exploiter)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CyberSurface.copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            NeonPurple.copy(alpha = 0.7f),
                            NeonCyan.copy(alpha = 0.5f)
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .testTag("developer_credentials_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Developer Clearance",
                            tint = NeonPurple,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "DEVELOPER SPECIFICATION",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "C9-SHANICE Exploiter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = TerminalGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "ROOT ACCESS EXPLOITER",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TerminalGreen
                            )
                        }

                        Text(
                            text = "•",
                            fontSize = 10.sp,
                            color = TextMuted
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "CYBER ARCHITECT",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = NeonAmber
                            )
                        }
                    }
                }
            }
        }

        // Bottom Animated Progress & Boot Console
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bootLogSteps.getOrElse(bootStepIndex) { "SYSTEM READY" },
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isReady) NeonGreen else NeonCyan,
                    maxLines = 1
                )

                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tech Glowing Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CyberSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeonCyan, NeonPurple, NeonGreen)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Cryptographic Signature
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Signature",
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "AUTH PROTOCOL: SHANICE-SEC-9X // ALL RIGHTS RESERVED",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
