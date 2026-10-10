package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.WhatsAppApiProvider
import com.example.agent.WhatsAppPersona
import com.example.data.database.WhatsAppMessageLog
import com.example.data.database.WhatsAppRuleEntity
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AgentViewModel
import com.example.ui.viewmodel.WhatsAppSimChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val WhatsAppBrandGreen = Color(0xFF25D366)
val WhatsAppDarkGreen = Color(0xFF075E54)
val WhatsAppChatBubbleBot = Color(0xFF005C4B)
val WhatsAppChatBubbleUser = Color(0xFF202C33)

enum class WhatsAppSubSection(val title: String) {
    SANDBOX("💬 Chat Sandbox"),
    DIRECT_CHAT("🚀 Direct Chat"),
    API_CONFIG("🔑 API Gateway"),
    PERSONA("🤖 Persona & Shop"),
    RULES("⚡ Trigger Rules"),
    NOTIF_SETUP("🔔 Background Service"),
    LOGS("📜 Logs")
}

@Composable
fun WhatsAppAgentScreen(
    viewModel: AgentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rules by viewModel.whatsAppRules.collectAsState()
    val logs by viewModel.whatsAppLogs.collectAsState()
    val isAutoReply by viewModel.isWhatsAppAutoReply.collectAsState()
    val currentPersona by viewModel.whatsAppPersona.collectAsState()
    val apiKey by viewModel.whatsAppApiKey.collectAsState()
    val phoneNumberId by viewModel.whatsAppPhoneNumberId.collectAsState()
    val webhookUrl by viewModel.whatsAppWebhookUrl.collectAsState()
    val apiProvider by viewModel.whatsAppProvider.collectAsState()
    val customPrompt by viewModel.whatsAppCustomPrompt.collectAsState()
    val apiStatus by viewModel.whatsAppApiStatus.collectAsState()
    val simChatMessages by viewModel.simulatedChatMessages.collectAsState()

    var activeSubSection by remember { mutableStateOf(WhatsAppSubSection.SANDBOX) }

    // Direct Chat state
    var directNumber by remember { mutableStateOf("") }
    var directMessage by remember { mutableStateOf("") }

    // Sandbox message state
    var simInputText by remember { mutableStateOf("") }

    // API settings local editing state
    var localApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var localPhoneId by remember(phoneNumberId) { mutableStateOf(phoneNumberId) }
    var localWebhook by remember(webhookUrl) { mutableStateOf(webhookUrl) }
    var localProvider by remember(apiProvider) { mutableStateOf(apiProvider) }
    var showApiKey by remember { mutableStateOf(false) }

    // Persona local editing state
    var localPersona by remember(currentPersona) { mutableStateOf(currentPersona) }
    var localCustomPrompt by remember(customPrompt) { mutableStateOf(customPrompt) }

    // Rule adding state
    var isAddingRule by remember { mutableStateOf(false) }
    var newRuleKeyword by remember { mutableStateOf("") }
    var newRuleReply by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. WhatsApp Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppBrandGreen.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(WhatsAppBrandGreen.copy(alpha = 0.2f))
                                    .border(2.dp, WhatsAppBrandGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "WhatsApp AI",
                                    tint = WhatsAppBrandGreen,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "WHATSAPP AI AGENT",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isAutoReply) WhatsAppBrandGreen.copy(alpha = 0.2f) else CyberSurfaceVariant)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isAutoReply) "AUTO-REPLY ON" else "PAUSED",
                                            color = if (isAutoReply) WhatsAppBrandGreen else TextMuted,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "Autonomous 24/7 Auto-Responder & Customer Agent",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = isAutoReply,
                            onCheckedChange = { viewModel.toggleWhatsAppAutoReply(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF001A22),
                                checkedTrackColor = WhatsAppBrandGreen,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = CyberSurfaceVariant
                            ),
                            modifier = Modifier.testTag("whatsapp_autoreply_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Stats & Status Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickStatChip(
                            label = "ENGINE",
                            value = if (apiProvider == WhatsAppApiProvider.LOCAL_SMART_AI) "Local AI" else "Cloud API",
                            color = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                        QuickStatChip(
                            label = "PERSONA",
                            value = currentPersona.displayName.take(12),
                            color = NeonPurple,
                            modifier = Modifier.weight(1f)
                        )
                        QuickStatChip(
                            label = "RULES",
                            value = "${rules.size} Active",
                            color = WhatsAppBrandGreen,
                            modifier = Modifier.weight(1f)
                        )
                        QuickStatChip(
                            label = "LOGS",
                            value = "${logs.size} Msg",
                            color = NeonAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Sub-Section Navigation Tabs
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(WhatsAppSubSection.entries.toTypedArray()) { section ->
                    val isSelected = activeSubSection == section
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeSubSection = section },
                        label = {
                            Text(
                                text = section.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WhatsAppBrandGreen.copy(alpha = 0.25f),
                            selectedLabelColor = WhatsAppBrandGreen,
                            containerColor = CyberSurfaceVariant,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) WhatsAppBrandGreen else CyberCardBorder
                        ),
                        modifier = Modifier.testTag("wa_tab_${section.name.lowercase()}")
                    )
                }
            }
        }

        // 3. Render Active Sub-Section
        when (activeSubSection) {
            WhatsAppSubSection.SANDBOX -> {
                item {
                    WhatsAppChatSandboxCard(
                        messages = simChatMessages,
                        inputText = simInputText,
                        onInputChanged = { simInputText = it },
                        onSend = {
                            if (simInputText.isNotBlank()) {
                                viewModel.simulateWhatsAppMessage(simInputText)
                                simInputText = ""
                            }
                        },
                        onClearChat = { viewModel.clearWhatsAppSimulatedChat() },
                        onQuickPromptClick = { prompt ->
                            viewModel.simulateWhatsAppMessage(prompt)
                        }
                    )
                }
            }

            WhatsAppSubSection.DIRECT_CHAT -> {
                item {
                    WhatsAppDirectChatCard(
                        directNumber = directNumber,
                        directMessage = directMessage,
                        onNumberChanged = { directNumber = it },
                        onMessageChanged = { directMessage = it },
                        onSendDirect = {
                            viewModel.openDirectWhatsAppChat(directNumber, directMessage)
                        },
                        onShare = {
                            viewModel.shareToWhatsApp(directMessage.ifBlank { "Assalam-o-Alaikum from C9-SHANICE AI Agent." })
                        }
                    )
                }
            }

            WhatsAppSubSection.API_CONFIG -> {
                item {
                    WhatsAppApiGatewayCard(
                        apiKey = localApiKey,
                        phoneNumberId = localPhoneId,
                        webhookUrl = localWebhook,
                        provider = localProvider,
                        showApiKey = showApiKey,
                        apiStatus = apiStatus,
                        onApiKeyChanged = { localApiKey = it },
                        onPhoneIdChanged = { localPhoneId = it },
                        onWebhookChanged = { localWebhook = it },
                        onProviderChanged = { localProvider = it },
                        onToggleShowKey = { showApiKey = !showApiKey },
                        onSaveConfig = {
                            viewModel.saveWhatsAppConfig(
                                apiKey = localApiKey,
                                phoneNumberId = localPhoneId,
                                webhookUrl = localWebhook,
                                provider = localProvider,
                                persona = localPersona,
                                customPrompt = localCustomPrompt,
                                isAutoReply = isAutoReply
                            )
                        },
                        onTestConnection = {
                            viewModel.testWhatsAppApiConnection()
                        }
                    )
                }
            }

            WhatsAppSubSection.PERSONA -> {
                item {
                    WhatsAppPersonaCard(
                        selectedPersona = localPersona,
                        customPrompt = localCustomPrompt,
                        onPersonaSelected = { localPersona = it },
                        onCustomPromptChanged = { localCustomPrompt = it },
                        onSave = {
                            viewModel.saveWhatsAppConfig(
                                apiKey = localApiKey,
                                phoneNumberId = localPhoneId,
                                webhookUrl = localWebhook,
                                provider = localProvider,
                                persona = localPersona,
                                customPrompt = localCustomPrompt,
                                isAutoReply = isAutoReply
                            )
                        }
                    )
                }
            }

            WhatsAppSubSection.RULES -> {
                item {
                    WhatsAppRulesCard(
                        rules = rules,
                        isAddingRule = isAddingRule,
                        newRuleKeyword = newRuleKeyword,
                        newRuleReply = newRuleReply,
                        onToggleAddRule = { isAddingRule = !isAddingRule },
                        onKeywordChanged = { newRuleKeyword = it },
                        onReplyChanged = { newRuleReply = it },
                        onSaveRule = {
                            if (newRuleKeyword.isNotBlank() && newRuleReply.isNotBlank()) {
                                viewModel.addWhatsAppRule(newRuleKeyword, newRuleReply)
                                newRuleKeyword = ""
                                newRuleReply = ""
                                isAddingRule = false
                            }
                        },
                        onLoadDefaults = { viewModel.loadDefaultWhatsAppBusinessRules() },
                        onToggleRule = { id, enabled -> viewModel.toggleWhatsAppRule(id, enabled) },
                        onDeleteRule = { id -> viewModel.deleteWhatsAppRule(id) }
                    )
                }
            }

            WhatsAppSubSection.NOTIF_SETUP -> {
                item {
                    WhatsAppBackgroundSetupCard(context = context)
                }
            }

            WhatsAppSubSection.LOGS -> {
                item {
                    WhatsAppLogsCard(
                        logs = logs,
                        onClearLogs = { viewModel.clearWhatsAppLogs() }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberBlack)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            color = TextMuted,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

// -------------------------------------------------------------
// 1. LIVE CHAT SANDBOX COMPONENT
// -------------------------------------------------------------
@Composable
fun WhatsAppChatSandboxCard(
    messages: List<WhatsAppSimChatMessage>,
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onClearChat: () -> Unit,
    onQuickPromptClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppBrandGreen.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(WhatsAppBrandGreen)
                    )
                    Text(
                        text = "LIVE WHATSAPP SIMULATOR",
                        style = MaterialTheme.typography.labelMedium,
                        color = WhatsAppBrandGreen,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Clear Chat", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Test karein ke customer ke message par bot kya jawab deta hai:",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Test Chips
            Text(text = "Quick Test Queries:", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val testQueries = listOf(
                    "Salam, price kya hai?",
                    "Cash on delivery hai?",
                    "Order kaise karein?",
                    "Delivery charges kitne hain?",
                    "Shop location kahan hai?",
                    "Urgent rabta karein"
                )
                items(testQueries) { query ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberSurfaceVariant)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(6.dp))
                            .clickable { onQuickPromptClick(query) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = query, fontSize = 10.sp, color = NeonCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chat conversation box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C1317)) // WhatsApp dark chat background
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        WhatsAppChatBubble(message = msg)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChanged,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wa_sim_input"),
                    placeholder = { Text("Client message likhein...", fontSize = 11.sp, color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WhatsAppBrandGreen,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CyberSurfaceVariant,
                        unfocusedContainerColor = CyberSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() })
                )

                Button(
                    onClick = onSend,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppBrandGreen,
                        contentColor = Color(0xFF001A22)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("wa_sim_send_btn")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun WhatsAppChatBubble(message: WhatsAppSimChatMessage) {
    val isClient = message.sender == "client"
    val timeStr = SimpleDateFormat("HH:mm", Locale.US).format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isClient) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = if (isClient) 10.dp else 2.dp,
                        bottomEnd = if (isClient) 2.dp else 10.dp
                    )
                )
                .background(if (isClient) WhatsAppChatBubbleUser else WhatsAppChatBubbleBot)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column {
                if (!isClient && message.reasoning.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF00382E))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "🤖 ${message.reasoning}",
                                fontSize = 8.sp,
                                color = WhatsAppBrandGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 9.sp,
                        color = Color(0xFF8696A0),
                        fontFamily = FontFamily.Monospace
                    )
                    if (!isClient) {
                        // Double blue tick
                        Text(
                            text = "✓✓",
                            fontSize = 9.sp,
                            color = Color(0xFF53BDEB),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. DIRECT CHAT & BROADCAST COMPONENT
// -------------------------------------------------------------
@Composable
fun WhatsAppDirectChatCard(
    directNumber: String,
    directMessage: String,
    onNumberChanged: (String) -> Unit,
    onMessageChanged: (String) -> Unit,
    onSendDirect: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = WhatsAppBrandGreen, modifier = Modifier.size(16.dp))
                Text(
                    text = "DIRECT WHATSAPP CHAT (BINA NUMBER SAVE KIYE)",
                    style = MaterialTheme.typography.labelMedium,
                    color = WhatsAppBrandGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Kisi bhi number par bina phone contact save kiye foran chat open karein:",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Country prefix quick selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("+92", "🇵🇰 PK"),
                    Pair("+1", "🇺🇸 US"),
                    Pair("+44", "🇬🇧 UK"),
                    Pair("+971", "🇦🇪 UAE"),
                    Pair("+966", "🇸🇦 KSA")
                ).forEach { (code, flag) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberSurfaceVariant)
                            .clickable {
                                if (!directNumber.startsWith(code)) {
                                    onNumberChanged(code + directNumber.removePrefix("+").removePrefix("92").removePrefix("0"))
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(text = "$flag $code", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = directNumber,
                onValueChange = onNumberChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("direct_phone_field"),
                placeholder = { Text("Phone Number (+923001234567 ya 0300...)", color = TextMuted, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhatsAppBrandGreen,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CyberSurfaceVariant,
                    unfocusedContainerColor = CyberSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = directMessage,
                onValueChange = onMessageChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("direct_msg_field"),
                placeholder = { Text("Message likhein ya AI Draft select karein...", color = TextMuted, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhatsAppBrandGreen,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CyberSurfaceVariant,
                    unfocusedContainerColor = CyberSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // AI Draft Presets
            Text(text = "Quick AI Drafts:", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val drafts = listOf(
                    Pair("👋 Welcome", "Assalam-o-Alaikum! Hope you are doing well. Reaching out via C9-SHANICE AI Agent."),
                    Pair("💰 Quotation", "Assalam-o-Alaikum! Please find your requested product price quotation and package details attached."),
                    Pair("📦 Dispatched", "Salam! Aapka order dispatch kar diya gaya hai. Tracking ID jald SMS/WhatsApp kar di jayegi."),
                    Pair("⏰ Follow-up", "Hello! Just checking in to see if you had any questions regarding our previous discussion.")
                )
                items(drafts) { (label, text) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberSurfaceVariant)
                            .clickable { onMessageChanged(text) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = label, fontSize = 10.sp, color = NeonCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberSurfaceVariant,
                        contentColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share to Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSendDirect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppBrandGreen,
                        contentColor = Color(0xFF001A22)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_send_direct_wa")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. API GATEWAY & KEY REPLACEMENT COMPONENT
// -------------------------------------------------------------
@Composable
fun WhatsAppApiGatewayCard(
    apiKey: String,
    phoneNumberId: String,
    webhookUrl: String,
    provider: WhatsAppApiProvider,
    showApiKey: Boolean,
    apiStatus: String,
    onApiKeyChanged: (String) -> Unit,
    onPhoneIdChanged: (String) -> Unit,
    onWebhookChanged: (String) -> Unit,
    onProviderChanged: (WhatsAppApiProvider) -> Unit,
    onToggleShowKey: () -> Unit,
    onSaveConfig: () -> Unit,
    onTestConnection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = "API GATEWAY & KEY REPLACEMENT",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "READY FOR KEY",
                        color = NeonGreen,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Explanatory note in Urdu & English
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberBlack)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "ℹ️ API Key Replacement Notice:",
                        color = NeonAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                        Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Filhal app bina kisi key ke 100% smart local intelligence aur Android notification auto-reply par chal rahi hai. Jab aap apni Meta WhatsApp Cloud API ya koi bhi API Key dein ge, yahan paste kar ke 'Save & Replace' dabayein. Seamlessly switch ho jayega!",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Select Gateway Provider:",
                fontSize = 11.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(WhatsAppApiProvider.entries.toTypedArray()) { prov ->
                    val isSelected = provider == prov
                    FilterChip(
                        selected = isSelected,
                        onClick = { onProviderChanged(prov) },
                        label = {
                            Text(
                                text = prov.displayName,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // API Key field
            Text(text = "WhatsApp API Key / Access Token:", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("field_wa_api_key"),
                placeholder = { Text("Paste Meta Bearer Token or Twilio API Key here...", color = TextMuted, fontSize = 11.sp) },
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleShowKey) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Key",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CyberSurfaceVariant,
                    unfocusedContainerColor = CyberSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Phone Number ID field
            if (provider == WhatsAppApiProvider.META_CLOUD_API) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Meta Phone Number ID:", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = phoneNumberId,
                    onValueChange = onPhoneIdChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 104593820194821", color = TextMuted, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CyberSurfaceVariant,
                        unfocusedContainerColor = CyberSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            // Webhook field
            if (provider == WhatsAppApiProvider.CUSTOM_WEBHOOK) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Webhook / Server Endpoint URL:", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = webhookUrl,
                    onValueChange = onWebhookChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://my-domain.com/api/whatsapp-reply", color = TextMuted, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CyberSurfaceVariant,
                        unfocusedContainerColor = CyberSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status message
            Text(
                text = "Gateway Status: $apiStatus",
                color = if (apiStatus.contains("error", ignoreCase = true) || apiStatus.contains("empty", ignoreCase = true)) NeonAmber else NeonGreen,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestConnection,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberSurfaceVariant,
                        contentColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSaveConfig,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = Color(0xFF001A22)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_wa_config")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Replace", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. PERSONA & CUSTOM BUSINESS KNOWLEDGE BASE COMPONENT
// -------------------------------------------------------------
@Composable
fun WhatsAppPersonaCard(
    selectedPersona: WhatsAppPersona,
    customPrompt: String,
    onPersonaSelected: (WhatsAppPersona) -> Unit,
    onCustomPromptChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "BOT PERSONA & CUSTOM SHOP KNOWLEDGE",
                style = MaterialTheme.typography.labelMedium,
                color = NeonPurple,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Choose conversation tone and provide your business policies:",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(WhatsAppPersona.entries.toTypedArray()) { persona ->
                    val isSelected = selectedPersona == persona
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPersonaSelected(persona) },
                        label = {
                            Text(
                                text = persona.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurple.copy(alpha = 0.25f),
                            selectedLabelColor = NeonPurple,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Behavior: ${selectedPersona.systemInstruction}",
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Custom Business Profile & Rules (Urdu / English):",
                fontSize = 11.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = customPrompt,
                onValueChange = onCustomPromptChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wa_custom_prompt_field"),
                placeholder = {
                    Text(
                        text = "e.g. Hamari shop ka naam 'Zeey Boutique' hai. Gents kurta 2500 PKR, ladies suit 3500 PKR. Delivery 200 PKR (3 days). EasyPaisa & COD available...",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CyberSurfaceVariant,
                    unfocusedContainerColor = CyberSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Persona & Business Knowledge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 5. TRIGGER RULES COMPONENT
// -------------------------------------------------------------
@Composable
fun WhatsAppRulesCard(
    rules: List<WhatsAppRuleEntity>,
    isAddingRule: Boolean,
    newRuleKeyword: String,
    newRuleReply: String,
    onToggleAddRule: () -> Unit,
    onKeywordChanged: (String) -> Unit,
    onReplyChanged: (String) -> Unit,
    onSaveRule: () -> Unit,
    onLoadDefaults: () -> Unit,
    onToggleRule: (Long, Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUTO-REPLY TRIGGER RULES (${rules.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = WhatsAppBrandGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onLoadDefaults) {
                        Text("Reset Presets", color = NeonCyan, fontSize = 10.sp)
                    }
                    IconButton(
                        onClick = onToggleAddRule,
                        modifier = Modifier.testTag("btn_toggle_add_rule")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = WhatsAppBrandGreen)
                    }
                }
            }

            if (isAddingRule) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurfaceVariant)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newRuleKeyword,
                        onValueChange = onKeywordChanged,
                        placeholder = { Text("Keyword Trigger (e.g. price, order, location)", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp)
                    )
                    OutlinedTextField(
                        value = newRuleReply,
                        onValueChange = onReplyChanged,
                        placeholder = { Text("Automated Reply Message...", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onSaveRule,
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppBrandGreen),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("SAVE RULE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001A22))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (rules.isEmpty()) {
                Text(
                    text = "No custom rules. Click 'Reset Presets' to load standard customer greeting & pricing rules.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rules.forEach { rule ->
                        WhatsAppRuleRow(
                            rule = rule,
                            onToggle = { onToggleRule(rule.id, it) },
                            onDelete = { onDeleteRule(rule.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppRuleRow(
    rule: WhatsAppRuleEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberBlack)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Trigger: \"${rule.keyword}\"",
                color = WhatsAppBrandGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Reply: ${rule.response}",
                color = TextPrimary,
                fontSize = 11.sp,
                maxLines = 2
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF001A22),
                    checkedTrackColor = WhatsAppBrandGreen
                )
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 6. BACKGROUND SETUP COMPONENT
// -------------------------------------------------------------
@Composable
fun WhatsAppBackgroundSetupCard(context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = WhatsAppBrandGreen, modifier = Modifier.size(18.dp))
                Text(
                    text = "24/7 NATIVE BACKGROUND AUTO-REPLY",
                    style = MaterialTheme.typography.labelMedium,
                    color = WhatsAppBrandGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Android Notification Listener Service ke zariye jab bhi mobile par WhatsApp message aata hai, C9-SHANICE screen open kiye baghair background mein automated inline reply bhej sakta hai.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallback = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try { context.startActivity(fallback) } catch (e2: Exception) {}
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = WhatsAppBrandGreen,
                    contentColor = Color(0xFF001A22)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Notification Access Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 7. MESSAGE LOGS COMPONENT
// -------------------------------------------------------------
@Composable
fun WhatsAppLogsCard(
    logs: List<WhatsAppMessageLog>,
    onClearLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MESSAGE INTERACTION LOGS (${logs.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                if (logs.isNotEmpty()) {
                    TextButton(onClick = onClearLogs) {
                        Text("Clear", color = NeonRed, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (logs.isEmpty()) {
                Text(
                    text = "Abhi tak koi message log nahi hua. Chat Sandbox mein test karein ya notification listener enable karein.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    logs.take(15).forEach { log ->
                        WhatsAppLogItemRow(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppLogItemRow(log: WhatsAppMessageLog) {
    val dateStr = SimpleDateFormat("HH:mm - dd MMM", Locale.US).format(Date(log.timestamp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberBlack)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "From: ${log.sender}",
                color = WhatsAppBrandGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateStr,
                color = TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "📩 \"${log.incomingText}\"",
            color = TextSecondary,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "🤖 Reply: ${log.replyText}",
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
