package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AgentGuidelinesDialog(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "dialog_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Main Dialog Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.7f),
                                NeonPurple.copy(alpha = 0.6f),
                                NeonCyan.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .testTag("agent_guidelines_dialog"),
                color = Color(0xFF0C121F), // Dark cyber background matching app theme
                tonalElevation = 12.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    // Top Right Close Button (Circle with X)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.dp, NeonRed.copy(alpha = 0.5f), CircleShape)
                            .testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Guidelines Dialog",
                            tint = NeonRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))

                        // Top Center Emblem / Badge (matching reference layout with cyberpunk luxury aesthetic)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .scale(pulseScale)
                                .size(110.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            NeonPurple.copy(alpha = 0.35f),
                                            NeonCyan.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(NeonCyan, NeonPurple, NeonAmber)
                                    ),
                                    shape = RoundedCornerShape(32.dp)
                                )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Security Core",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "C9-SHANICE",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Every Feature Here",
                                    fontSize = 9.sp,
                                    color = NeonAmber,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Welcome Statement
                        Text(
                            text = "Welcome to C9-SHANICE AI Agent – your all-in-one app!",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Here you can access all the tools you need in one place.",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // GUIDELINES Card (matching reference layout)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = NeonCyan.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF131D2E)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Guidelines Header Pill
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "GUIDELINES",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = NeonCyan,
                                        letterSpacing = 1.5.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Guideline Item 1
                                GuidelineRowItem(
                                    icon = Icons.Default.CheckCircle,
                                    iconColor = NeonGreen,
                                    text = "Use the app responsibly and ethically."
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 0.8.dp,
                                    color = Color(0xFF1E293B)
                                )

                                // Guideline Item 2
                                GuidelineRowItem(
                                    icon = Icons.Default.Security,
                                    iconColor = NeonCyan,
                                    text = "Do not misuse any tool or feature."
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 0.8.dp,
                                    color = Color(0xFF1E293B)
                                )

                                // Guideline Item 3
                                GuidelineRowItem(
                                    icon = Icons.Default.Lock,
                                    iconColor = NeonPurple,
                                    text = "Your privacy and data are our top priority."
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 0.8.dp,
                                    color = Color(0xFF1E293B)
                                )

                                // Guideline Item 4
                                GuidelineRowItem(
                                    icon = Icons.Default.Star,
                                    iconColor = NeonAmber,
                                    text = "Provide honest feedback to help us improve."
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 0.8.dp,
                                    color = Color(0xFF1E293B)
                                )

                                // Guideline Item 5
                                GuidelineRowItem(
                                    icon = Icons.Default.Person,
                                    iconColor = NeonRed,
                                    text = "Enjoy using C9-SHANICE AI Agent and share it with others."
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottom 3 Info Cards (APP NAME, DEVELOPERS, CONTACT EMAIL)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Card 1: APP NAME
                            InfoPillCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Person,
                                iconColor = NeonPurple,
                                headerTitle = "APP NAME",
                                mainContent = "C9-SHANICE AI Agent",
                                borderColor = NeonPurple.copy(alpha = 0.4f),
                                subContent = null
                            )

                            // Card 2: DEVELOPERS
                            InfoPillCard(
                                modifier = Modifier.weight(1.3f),
                                icon = Icons.Default.Code,
                                iconColor = NeonCyan,
                                headerTitle = "DEVELOPERS",
                                mainContent = "C9-SHANICE EXPLOITER\nTEAM BLACK HAT",
                                borderColor = NeonCyan.copy(alpha = 0.4f),
                                subContent = "Lead: Muhamad Zeeshan"
                            )

                            // Card 3: CONTACT EMAIL
                            InfoPillCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Email,
                                iconColor = NeonGreen,
                                headerTitle = "CONTACT EMAIL",
                                mainContent = "—", // User requested: "Email contact. Filhal Khali hu."
                                borderColor = NeonGreen.copy(alpha = 0.4f),
                                subContent = "Not Configured"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bottom Thank You Footer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Thank you for choosing ",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                            Text(
                                text = "C9-SHANICE AI Agent! ⚡",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Acknowledge Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("guidelines_acknowledge_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color(0xFF001A22)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ACKNOWLEDGE & PROCEED",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidelineRowItem(
    icon: ImageVector,
    iconColor: Color,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f))
                .border(1.dp, iconColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            color = TextPrimary,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun InfoPillCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    headerTitle: String,
    mainContent: String,
    borderColor: Color,
    subContent: String?
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131D2E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f))
                    .border(1.dp, iconColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = headerTitle,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = mainContent,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )

            if (!subContent.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subContent,
                    fontSize = 8.5.sp,
                    color = NeonAmber,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
