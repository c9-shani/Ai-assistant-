package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TelemetryGaugeCard
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AgentViewModel

@Composable
fun DeviceTelemetryScreen(
    viewModel: AgentViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DEVICE TELEMETRY & HARDWARE",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Live mobile sensors and resource monitors",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshTelemetry() },
                    modifier = Modifier.testTag("telemetry_screen_refresh")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Telemetry",
                        tint = NeonCyan
                    )
                }
            }
        }

        // 1. RAM Gauge Card
        item {
            TelemetryGaugeCard(
                title = "SYSTEM RAM ALLOCATION",
                value = "${telemetry.ramUsedMb} MB / ${telemetry.ramTotalMb} MB",
                subtitle = "Available: ${telemetry.ramAvailableMb} MB free memory",
                percentage = telemetry.ramPercent,
                accentColor = NeonCyan,
                icon = Icons.Default.Memory,
                modifier = Modifier.testTag("gauge_ram")
            )
        }

        // 2. Battery Telemetry Card
        item {
            TelemetryGaugeCard(
                title = "BATTERY & POWER UNIT",
                value = "${telemetry.batteryPercent}% ${if (telemetry.isCharging) "(Charging)" else "(Discharging)"}",
                subtitle = "Thermal: ${telemetry.batteryTempC}°C | Health: ${telemetry.batteryHealth}",
                percentage = telemetry.batteryPercent,
                accentColor = if (telemetry.batteryPercent > 20) NeonGreen else NeonAmber,
                icon = Icons.Default.BatteryChargingFull,
                modifier = Modifier.testTag("gauge_battery")
            )
        }

        // 3. Storage Partition Card
        item {
            TelemetryGaugeCard(
                title = "INTERNAL FLASH STORAGE",
                value = "${telemetry.storageUsedGb} GB / ${telemetry.storageTotalGb} GB",
                subtitle = "Capacity partition /data/user/0",
                percentage = telemetry.storagePercent,
                accentColor = NeonPurple,
                icon = Icons.Default.SdStorage,
                modifier = Modifier.testTag("gauge_storage")
            )
        }

        // 4. Network Status Card
        item {
            TelemetryGaugeCard(
                title = "NETWORK & GATEWAY LATENCY",
                value = if (telemetry.isConnected) "${telemetry.networkType} (Online)" else "Offline",
                subtitle = if (telemetry.latencyMs > 0) "Ping RTT: ${telemetry.latencyMs} ms to DNS" else "Active gateway link",
                percentage = if (telemetry.isConnected) 90 else 0,
                accentColor = NeonAmber,
                icon = Icons.Default.NetworkCheck,
                modifier = Modifier.testTag("gauge_network")
            )
        }

        // 5. Quick Optimization Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MOBILE HARDWARE CONTROLS",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.executeTerminal("clean")
                                viewModel.refreshTelemetry()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_purge_ram"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan.copy(alpha = 0.2f),
                                contentColor = NeonCyan
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trim RAM", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.executeTerminal("net")
                                viewModel.refreshTelemetry()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_probe_latency"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonPurple.copy(alpha = 0.2f),
                                contentColor = NeonPurple
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ping Latency", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.processVoiceCommand("torch") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_toggle_torch"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB800).copy(alpha = 0.2f),
                                contentColor = Color(0xFFFFB800)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FlashlightOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Flashlight", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.processVoiceCommand("vibrate") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_test_vibrate"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen.copy(alpha = 0.2f),
                                contentColor = NeonGreen
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Haptic Vibe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6. Device Specs Breakdown Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HOST PLATFORM SPECIFICATIONS",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val specs = listOf(
                        "Device Model" to telemetry.deviceModel,
                        "OS Platform" to telemetry.androidVersion,
                        "CPU Cores" to "${telemetry.processorCores} Cores Active",
                        "System Uptime" to "${telemetry.uptimeHours} Hours",
                        "Agent Framework" to "C9-SHANICE Mobile Autonomous Architecture"
                    )

                    specs.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = value,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
