package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.RemoteConnectionState
import com.example.agent.RemoteSharedFile
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AgentViewModel

@Composable
fun RemoteControlScreen(
    viewModel: AgentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val remoteManager = viewModel.remoteDevice

    val connectionState by remoteManager.connectionState.collectAsState()
    val peerTelemetry by remoteManager.remotePeerTelemetry.collectAsState()
    val myDeviceCode by remoteManager.myDeviceCode.collectAsState()
    val mySessionPin by remoteManager.mySessionPin.collectAsState()
    val localIp by remoteManager.localIp.collectAsState()
    val listeningPort by remoteManager.listeningPort.collectAsState()
    val sharedFiles by remoteManager.sharedFiles.collectAsState()
    val sessionLogs by remoteManager.sessionLogs.collectAsState()

    val allowLaunch by remoteManager.allowRemoteAppLaunch.collectAsState()
    val allowFiles by remoteManager.allowFileTransfer.collectAsState()
    val allowHardware by remoteManager.allowHardwareActions.collectAsState()
    val allowClipboard by remoteManager.allowClipboardSync.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Controller (Connect), 1: Host (Share), 2: Data Transfer, 3: Session Logs
    var targetInviteCodeInput by remember { mutableStateOf("914-726-308") }
    var targetIpInput by remember { mutableStateOf(localIp) }
    var targetPortInput by remember { mutableStateOf(listeningPort.toString()) }
    var targetPinInput by remember { mutableStateOf("4920") }

    var customDirectiveInput by remember { mutableStateOf("") }
    var clipboardPushInput by remember { mutableStateOf("Sample password or shared URL link") }
    var newFileNameInput by remember { mutableStateOf("shared_note.txt") }
    var newFileContentInput by remember { mutableStateOf("C9-SHANICE: Remote mobile data synchronization verified!") }

    var inspectingFile by remember { mutableStateOf<RemoteSharedFile?>(null) }

    fun copyToClipboard(label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("remote_control_header_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, NeonCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Devices,
                                    contentDescription = "AnyDesk Remote Control",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ANYLINK // REMOTE NODE",
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                                Text(
                                    text = "AnyDesk Mobile Control & P2P Data Link",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // State pill
                        val stateColor = when (connectionState) {
                            RemoteConnectionState.CONNECTED -> NeonGreen
                            RemoteConnectionState.CONNECTING -> NeonAmber
                            RemoteConnectionState.LISTENING_HOST -> NeonPurple
                            else -> TextMuted
                        }
                        Box(
                            modifier = Modifier
                                .background(stateColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .border(1.dp, stateColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = connectionState.name,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = stateColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AnyDesk ki tarah doosre phone ko remote IP ya Invite Code se connect karein, screen/status dekhein, apps launch karein aur data share karein.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    if (connectionState == RemoteConnectionState.CONNECTED) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Target: ${peerTelemetry.deviceName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "IP: ${peerTelemetry.ipAddress} | Latency: ${peerTelemetry.pingLatencyMs}ms",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonGreen
                                )
                            }

                            Button(
                                onClick = { viewModel.terminateRemoteSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("terminate_session_button")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Disconnect", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Disconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Navigation Tabs
        item {
            val tabs = listOf("Controller (Viewer)", "Share This Phone (Host)", "Data Share", "Audit Logs")
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CyberSurface,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonCyan
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("remote_control_tabs")
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // TAB 0: CONTROLLER (CONNECT & CONTROL REMOTE DEVICE)
        if (selectedTab == 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CONNECT TO REMOTE MOBILE",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "Enter the 9-digit Invite Code or LAN IP of the target phone",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Target Invite Code
                        OutlinedTextField(
                            value = targetInviteCodeInput,
                            onValueChange = { targetInviteCodeInput = it },
                            label = { Text("Remote Invite Code (AnyDesk ID)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("remote_invite_code_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = targetIpInput,
                                onValueChange = { targetIpInput = it },
                                label = { Text("Target IP Address") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .weight(2f)
                                    .testTag("remote_ip_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = targetPinInput,
                                onValueChange = { targetPinInput = it },
                                label = { Text("PIN") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("remote_pin_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val port = targetPortInput.toIntOrNull() ?: 8765
                                    viewModel.connectRemoteDevice(
                                        targetIp = targetIpInput,
                                        port = port,
                                        inviteCode = targetInviteCodeInput,
                                        pin = targetPinInput
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("connect_remote_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Connect Remote", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.startRemoteSandbox("Samsung Galaxy S24 Ultra")
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("sandbox_demo_button")
                            ) {
                                Icon(Icons.Default.Airplay, contentDescription = null, tint = NeonPurple)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("1-Click Demo Sandbox", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // TARGET PHONE LIVE VIEWPORT & STATUS (ANYDESK SCREEN STREAM)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (connectionState == RemoteConnectionState.CONNECTED) NeonGreen else TextMuted, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TARGET DEVICE VIEWPORT (LIVE MIRROR)",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                            }

                            Text(
                                text = "${peerTelemetry.pingLatencyMs}ms • 60 FPS",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Virtual Mobile Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF070B12))
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Top status bar of target phone
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = peerTelemetry.deviceName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "🔋 ${peerTelemetry.batteryPct}%",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = NeonGreen
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "📶 5G",
                                            fontSize = 10.sp,
                                            color = NeonCyan
                                        )
                                    }
                                }

                                Divider(
                                    color = CyberCardBorder.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                // Current Active Viewport / App
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(CyberSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "CURRENT ACTIVE APP:",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = peerTelemetry.activeApp,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                        if (peerTelemetry.isTorchOn) {
                                            Text(
                                                text = "🔦 Flashlight: ACTIVE",
                                                fontSize = 10.sp,
                                                color = NeonAmber
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Android Navigation keys for target phone
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clickable { viewModel.simulateRemoteNavKey("BACK") }
                                            .background(CyberSurface, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 14.dp, vertical = 4.dp)
                                    ) {
                                        Text("◀ BACK", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clickable { viewModel.simulateRemoteNavKey("HOME") }
                                            .background(CyberSurface, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 14.dp, vertical = 4.dp)
                                    ) {
                                        Text("● HOME", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clickable { viewModel.simulateRemoteNavKey("RECENTS") }
                                            .background(CyberSurface, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 14.dp, vertical = 4.dp)
                                    ) {
                                        Text("■ RECENTS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clickable { viewModel.simulateRemoteNavKey("LOCK") }
                                            .background(CyberSurface, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 14.dp, vertical = 4.dp)
                                    ) {
                                        Text("🔒 LOCK", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = NeonAmber)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // REMOTE APP LAUNCHER (REMOTE CONTROL ACTIONS)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "REMOTE APP LAUNCHER",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "Tap any app to remotely trigger & open it on the target phone",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val remoteApps = listOf(
                            Pair("WhatsApp", "com.whatsapp"),
                            Pair("YouTube", "com.google.android.youtube"),
                            Pair("Settings", "com.android.settings"),
                            Pair("Camera", "com.google.android.GoogleCamera"),
                            Pair("Chrome", "com.android.chrome"),
                            Pair("Phone Dialer", "com.google.android.dialer"),
                            Pair("Gallery", "com.google.android.apps.photos"),
                            Pair("Files", "com.google.android.documentsui")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            remoteApps.take(4).forEach { (name, pkg) ->
                                Button(
                                    onClick = { viewModel.sendRemoteAppLaunch(pkg, name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                ) {
                                    Text(name, fontSize = 10.sp, color = TextPrimary, maxLines = 1)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            remoteApps.drop(4).forEach { (name, pkg) ->
                                Button(
                                    onClick = { viewModel.sendRemoteAppLaunch(pkg, name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                ) {
                                    Text(name, fontSize = 10.sp, color = TextPrimary, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            // REMOTE HARDWARE & TELEMETRY CONTROLS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "REMOTE HARDWARE & ALERTS",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.sendRemoteVibrateAlert() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("remote_vibrate_button")
                            ) {
                                Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ring / Vibrate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.sendRemoteTorchToggle() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("remote_torch_button")
                            ) {
                                Icon(Icons.Default.FlashlightOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Toggle Torch", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Remote Clipboard Push
                        Text(
                            text = "PUSH CLIPBOARD TEXT TO TARGET PHONE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = clipboardPushInput,
                                onValueChange = { clipboardPushInput = it },
                                placeholder = { Text("Enter text to copy on target phone...") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("remote_clipboard_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.sendRemoteClipboard(clipboardPushInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Send", tint = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Remote AI Agent Directive
                        Text(
                            text = "EXECUTE AI DIRECTIVE ON REMOTE AGENT",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = customDirectiveInput,
                                onValueChange = { customDirectiveInput = it },
                                placeholder = { Text("e.g. Clean RAM or Read battery...") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (customDirectiveInput.isNotBlank()) {
                                        viewModel.sendRemoteDirective(customDirectiveInput)
                                        customDirectiveInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send Directive", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: SHARE THIS PHONE (HOST MODE / ANYDESK SERVER)
        if (selectedTab == 1) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "THIS PHONE'S INVITE CODE",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )
                                Text(
                                    text = myDeviceCode,
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { copyToClipboard("Invite Code", myDeviceCode) },
                                    modifier = Modifier.testTag("copy_device_code_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = NeonCyan)
                                }
                                IconButton(
                                    onClick = { viewModel.regenerateRemoteInviteCode() },
                                    modifier = Modifier.testTag("regenerate_code_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = NeonAmber)
                                }
                            }
                        }

                        Divider(color = CyberCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Local Wi-Fi / IP Address", fontSize = 11.sp, color = TextMuted)
                                Text("$localIp : $listeningPort", fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Column {
                                Text("Temporary Session PIN", fontSize = 11.sp, color = TextMuted)
                                Text(mySessionPin, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NeonGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.startRemoteHostMode() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (connectionState == RemoteConnectionState.LISTENING_HOST) NeonGreen else NeonPurple
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_host_mode_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (connectionState == RemoteConnectionState.LISTENING_HOST) "Host Active (Listening...)" else "Start Host Mode (Allow Remote Access)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ANYDESK SECURITY & PERMISSIONS GUARD
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SECURITY & ACCESS CONTROL",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "Control what the remote controller is permitted to do on this phone",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow Remote App Launch", fontSize = 13.sp, color = TextPrimary)
                            Switch(
                                checked = allowLaunch,
                                onCheckedChange = { remoteManager.allowRemoteAppLaunch.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha = 0.3f))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow P2P File & Data Transfer", fontSize = 13.sp, color = TextPrimary)
                            Switch(
                                checked = allowFiles,
                                onCheckedChange = { remoteManager.allowFileTransfer.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha = 0.3f))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow Remote Hardware Alerts (Torch/Vibrate)", fontSize = 13.sp, color = TextPrimary)
                            Switch(
                                checked = allowHardware,
                                onCheckedChange = { remoteManager.allowHardwareActions.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha = 0.3f))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow Remote Clipboard Sync", fontSize = 13.sp, color = TextPrimary)
                            Switch(
                                checked = allowClipboard,
                                onCheckedChange = { remoteManager.allowClipboardSync.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }
        }

        // TAB 2: DATA & FILE SHARE
        if (selectedTab == 2) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CROSS-DEVICE DATA & FILE TRANSFER",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "Send files, documents, scripts or secret tokens directly to the remote phone",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Drop presets
                        Text("QUICK TRANSFER PRESETS:", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        newFileNameInput = "wifi_credentials.txt"
                                        newFileContentInput = "SSID: C9_OFFICE_5G\nPASS: StonicAI@2026\nAUTH: WPA3"
                                    }
                                    .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("📶 Wi-Fi Note", fontSize = 10.sp, color = NeonCyan)
                            }
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        newFileNameInput = "telemetry_dump.json"
                                        newFileContentInput = "{\n  \"battery\": 85,\n  \"ram\": 2400,\n  \"status\": \"NORMAL\"\n}"
                                    }
                                    .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("📊 Telemetry Log", fontSize = 10.sp, color = NeonGreen)
                            }
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        newFileNameInput = "contact_card.vcf"
                                        newFileContentInput = "BEGIN:VCARD\nFN:Shanice AI Support\nTEL:+923001234567\nEND:VCARD"
                                    }
                                    .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("👤 Contact Card", fontSize = 10.sp, color = NeonAmber)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newFileNameInput,
                            onValueChange = { newFileNameInput = it },
                            label = { Text("File Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("file_name_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newFileContentInput,
                            onValueChange = { newFileContentInput = it },
                            label = { Text("File Content / Payload Data") },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("file_content_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (newFileNameInput.isNotBlank()) {
                                    viewModel.sendRemoteFile(
                                        fileName = newFileNameInput,
                                        fileType = "Document",
                                        content = newFileContentInput
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("send_file_button")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send File to Connected Mobile", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SHARED FILES LIST
            item {
                Text(
                    text = "TRANSFERRED FILES (${sharedFiles.size})",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            items(sharedFiles) { file ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { inspectingFile = file }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.fileName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${file.fileType} • ${file.sizeBytes} bytes • From: ${file.sender}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        Row {
                            IconButton(onClick = { copyToClipboard(file.fileName, file.content) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: AUDIT & SESSION LOGS
        if (selectedTab == 3) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerminalBackground),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "P2P ENCRYPTED SESSION AUDIT LOG",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TerminalGreen
                            )
                            Text(
                                text = "${sessionLogs.size} Events",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Divider(color = CyberCardBorder, modifier = Modifier.padding(vertical = 8.dp))

                        sessionLogs.forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Text(
                                    text = "[${log.timeFormatted}] ",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )
                                Text(
                                    text = "<${log.type}> ",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = when (log.type) {
                                        "AUTH" -> NeonCyan
                                        "COMMAND" -> NeonPurple
                                        "ALERT" -> NeonRed
                                        "FILE" -> NeonGreen
                                        else -> TerminalGreen
                                    }
                                )
                                Text(
                                    text = log.message,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // INSPECT FILE DIALOG
    inspectingFile?.let { file ->
        AlertDialog(
            onDismissRequest = { inspectingFile = null },
            containerColor = CyberSurface,
            title = {
                Text(file.fileName, color = NeonCyan, fontFamily = FontFamily.Monospace)
            },
            text = {
                Column {
                    Text("Type: ${file.fileType} | Sender: ${file.sender}", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = file.content,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        copyToClipboard(file.fileName, file.content)
                        inspectingFile = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Copy Content", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectingFile = null }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }
}
