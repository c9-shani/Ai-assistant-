package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.AgentExecutor
import com.example.agent.AppLauncherManager
import com.example.agent.DeviceToolManager
import com.example.agent.InstalledAppInfo
import com.example.agent.RemoteConnectionState
import com.example.agent.RemoteDeviceManager
import com.example.agent.RemoteRole
import com.example.agent.RemoteSharedFile
import com.example.agent.WhatsAppApiProvider
import com.example.agent.WhatsAppBotManager
import com.example.agent.WhatsAppPersona
import com.example.data.database.WhatsAppConfigEntity
import com.example.data.database.WhatsAppRuleEntity
import com.example.data.database.WhatsAppMessageLog
import com.example.service.FloatingAgentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.data.api.GeminiAgentApi
import com.example.data.database.AgentDatabase
import com.example.data.database.MemoryEntity
import com.example.data.database.MissionEntity
import com.example.data.model.ActiveMission
import com.example.data.model.AgentState
import com.example.data.model.DeviceTelemetry
import com.example.data.model.LineType
import com.example.data.model.TerminalLine
import com.example.speech.AgentVoiceSpeaker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val sender: String, // "user" or "shanice"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class WhatsAppSimChatMessage(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val sender: String, // "client" or "bot"
    val text: String,
    val reasoning: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    val toolManager = DeviceToolManager(application)
    val appLauncher = AppLauncherManager(application)
    val speaker = AgentVoiceSpeaker(application)
    private val database = AgentDatabase.getDatabase(application)
    private var geminiApi = GeminiAgentApi()
    val whatsAppBot = WhatsAppBotManager(application, geminiApi)
    val remoteDevice = RemoteDeviceManager(application, toolManager, appLauncher, speaker)

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _isFloatingOverlayActive = MutableStateFlow(false)
    val isFloatingOverlayActive: StateFlow<Boolean> = _isFloatingOverlayActive.asStateFlow()

    private val _whatsAppRules = MutableStateFlow<List<WhatsAppRuleEntity>>(emptyList())
    val whatsAppRules: StateFlow<List<WhatsAppRuleEntity>> = _whatsAppRules.asStateFlow()

    private val _whatsAppLogs = MutableStateFlow<List<WhatsAppMessageLog>>(emptyList())
    val whatsAppLogs: StateFlow<List<WhatsAppMessageLog>> = _whatsAppLogs.asStateFlow()

    private val _whatsAppApiKey = MutableStateFlow("")
    val whatsAppApiKey: StateFlow<String> = _whatsAppApiKey.asStateFlow()

    private val _whatsAppPhoneNumberId = MutableStateFlow("")
    val whatsAppPhoneNumberId: StateFlow<String> = _whatsAppPhoneNumberId.asStateFlow()

    private val _whatsAppWebhookUrl = MutableStateFlow("")
    val whatsAppWebhookUrl: StateFlow<String> = _whatsAppWebhookUrl.asStateFlow()

    private val _whatsAppProvider = MutableStateFlow(WhatsAppApiProvider.LOCAL_SMART_AI)
    val whatsAppProvider: StateFlow<WhatsAppApiProvider> = _whatsAppProvider.asStateFlow()

    private val _whatsAppPersona = MutableStateFlow(WhatsAppPersona.CUSTOMER_SUPPORT)
    val whatsAppPersona: StateFlow<WhatsAppPersona> = _whatsAppPersona.asStateFlow()

    private val _whatsAppCustomPrompt = MutableStateFlow("")
    val whatsAppCustomPrompt: StateFlow<String> = _whatsAppCustomPrompt.asStateFlow()

    private val _isWhatsAppAutoReply = MutableStateFlow(true)
    val isWhatsAppAutoReply: StateFlow<Boolean> = _isWhatsAppAutoReply.asStateFlow()

    private val _whatsAppApiStatus = MutableStateFlow("Local Smart Engine Active (Ready for Custom Key)")
    val whatsAppApiStatus: StateFlow<String> = _whatsAppApiStatus.asStateFlow()

    private val _simulatedChatMessages = MutableStateFlow<List<WhatsAppSimChatMessage>>(
        listOf(
            WhatsAppSimChatMessage(
                sender = "bot",
                text = "Assalam-o-Alaikum! C9-SHANICE WhatsApp AI Agent active hai. Niche quick buttons ya text likh kar test karein.",
                reasoning = "System Init"
            )
        )
    )
    val simulatedChatMessages: StateFlow<List<WhatsAppSimChatMessage>> = _simulatedChatMessages.asStateFlow()

    private val _agentState = MutableStateFlow(AgentState.IDLE)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    private val _activeMission = MutableStateFlow<ActiveMission?>(null)
    val activeMission: StateFlow<ActiveMission?> = _activeMission.asStateFlow()

    private val _telemetry = MutableStateFlow(toolManager.getDeviceTelemetry())
    val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    private val _missionsHistory = MutableStateFlow<List<MissionEntity>>(emptyList())
    val missionsHistory: StateFlow<List<MissionEntity>> = _missionsHistory.asStateFlow()

    private val _memories = MutableStateFlow<List<MemoryEntity>>(emptyList())
    val memories: StateFlow<List<MemoryEntity>> = _memories.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "shanice",
                text = "C9-SHANICE Autonomous Mobile Agent online. System ready for task delegation or terminal execution. What is your directive?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _scriptCode = MutableStateFlow(
        """# Python/Agent Automation Script
# C9-SHANICE Mobile Sandbox
tasks = ["Audit Battery", "Clear Junk", "Verify Network"]
for t in tasks:
    print("Executing mobile routine: " + t)
print("System optimization complete.")
""".trimIndent()
    )
    val scriptCode: StateFlow<String> = _scriptCode.asStateFlow()

    private val _scriptOutput = MutableStateFlow("")
    val scriptOutput: StateFlow<String> = _scriptOutput.asStateFlow()

    private val _scriptLanguage = MutableStateFlow("Python")
    val scriptLanguage: StateFlow<String> = _scriptLanguage.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val executor = AgentExecutor(toolManager, database, speaker, geminiApi)
    private var missionJob: Job? = null

    init {
        // Collect DB flows
        viewModelScope.launch {
            database.missionDao().getAllMissions().collectLatest {
                _missionsHistory.value = it
            }
        }
        viewModelScope.launch {
            database.memoryDao().getAllMemories().collectLatest {
                _memories.value = it
            }
        }
        viewModelScope.launch {
            database.whatsAppDao().getAllRules().collectLatest { rules ->
                _whatsAppRules.value = rules
                if (rules.isEmpty()) {
                    loadDefaultWhatsAppBusinessRules()
                }
            }
        }
        viewModelScope.launch {
            database.whatsAppDao().getAllLogs().collectLatest {
                _whatsAppLogs.value = it
            }
        }
        viewModelScope.launch {
            database.whatsAppDao().getConfig().collectLatest { config ->
                val buildKey = try {
                    com.example.BuildConfig.WHATSAPP_API_KEY
                } catch (e: Exception) {
                    ""
                }
                val effectiveKey = if (config != null && config.apiKey.isNotBlank()) config.apiKey else buildKey

                _whatsAppApiKey.value = effectiveKey
                whatsAppBot.customApiKey = effectiveKey

                if (config != null) {
                    _whatsAppPhoneNumberId.value = config.phoneNumberId
                    whatsAppBot.phoneNumberId = config.phoneNumberId

                    _whatsAppWebhookUrl.value = config.webhookUrl
                    whatsAppBot.webhookUrl = config.webhookUrl

                    val prov = if (config.provider == WhatsAppApiProvider.LOCAL_SMART_AI.id && effectiveKey.isNotBlank()) {
                        WhatsAppApiProvider.META_CLOUD_API
                    } else {
                        WhatsAppApiProvider.fromId(config.provider)
                    }
                    _whatsAppProvider.value = prov
                    whatsAppBot.apiProvider = prov

                    val pers = WhatsAppPersona.fromName(config.persona)
                    _whatsAppPersona.value = pers
                    whatsAppBot.selectedPersona = pers

                    _whatsAppCustomPrompt.value = config.customBusinessPrompt
                    whatsAppBot.customBusinessPrompt = config.customBusinessPrompt

                    _isWhatsAppAutoReply.value = config.isAutoReplyEnabled
                    whatsAppBot.isAutoReplyActive = config.isAutoReplyEnabled

                    _whatsAppApiStatus.value = if (effectiveKey.isNotBlank()) {
                        "Meta WhatsApp Cloud API Active (Token Connected)"
                    } else {
                        "Local Smart AI Bridge Active (Ready for Custom Key)"
                    }
                } else if (effectiveKey.isNotBlank()) {
                    _whatsAppProvider.value = WhatsAppApiProvider.META_CLOUD_API
                    whatsAppBot.apiProvider = WhatsAppApiProvider.META_CLOUD_API
                    _whatsAppApiStatus.value = "Meta WhatsApp Cloud API Active (Token Connected)"
                }
            }
        }

        // Initialize terminal with welcome banner
        viewModelScope.launch {
            val initial = mutableListOf(
                TerminalLine(type = LineType.SYSTEM, text = "=== C9-SHANICE AUTONOMOUS MOBILE OS v3.2 ==="),
                TerminalLine(type = LineType.SYSTEM, text = "Target: Android Mobile Runtime Environment"),
                TerminalLine(type = LineType.SYSTEM, text = "Stonic AI Mobile Architecture Protocol Active"),
                TerminalLine(type = LineType.TOOL, text = "Type 'help' for shell commands or 'agent <goal>' to dispatch autonomous agent."),
                TerminalLine(type = LineType.OUTPUT, text = "")
            )
            _terminalLines.value = initial
            refreshTelemetry()
            loadInstalledApps()
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = appLauncher.getInstalledApps()
        }
    }

    fun launchApp(packageName: String) {
        val success = appLauncher.launchAppByPackage(packageName)
        if (success) {
            speaker.speak("Launching app.")
        }
    }

    fun processVoiceCommand(rawCommand: String) {
        if (rawCommand.isBlank()) return
        val command = rawCommand.trim()
        val lower = command.lowercase(java.util.Locale.ROOT)

        _chatMessages.value = _chatMessages.value + ChatMessage(sender = "user", text = command)

        viewModelScope.launch {
            // WhatsApp Commands
            if (lower.contains("whatsapp auto reply") || lower.contains("whatsapp autoreply") || lower.contains("whatsapp bot")) {
                val turnOn = !lower.contains("off") && !lower.contains("band") && !lower.contains("stop")
                toggleWhatsAppAutoReply(turnOn)
                val msg = if (turnOn) "WhatsApp Auto-Reply agent activated." else "WhatsApp Auto-Reply paused."
                _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = "✓ $msg")
                speaker.speak(msg)
                return@launch
            }

            // Remote Device / AnyDesk Mobile Commands
            if (lower.contains("remote control") || lower.contains("anydesk") || lower.contains("remote link") || lower.contains("remote host")) {
                if (lower.contains("host") || lower.contains("share")) {
                    remoteDevice.startHostMode()
                    val msg = "Remote Host mode active. Device Code: ${remoteDevice.myDeviceCode.value}"
                    _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = "✓ $msg")
                    return@launch
                } else {
                    remoteDevice.startSandboxConnection()
                    val msg = "Connected to Remote Device sandbox. Ready for control."
                    _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = "✓ $msg")
                    return@launch
                }
            }

            if (lower.contains("vibrate remote") || lower.contains("ring remote")) {
                remoteDevice.sendRemoteVibrationAlert()
                _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = "✓ Triggered remote phone alert.")
                return@launch
            }

            // 1. App Launching / System Settings
            if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ") ||
                lower.endsWith(" kholo") || lower.endsWith(" chalao") || lower.contains("open karo") ||
                lower.contains("camera") || lower.contains("settings") || lower.contains("whatsapp") ||
                lower.contains("youtube") || lower.contains("chrome") || lower.contains("calculator") ||
                lower.contains("gallery") || lower.contains("clock") || lower.contains("maps") ||
                lower.contains("wifi") || lower.contains("bluetooth")
            ) {
                val (success, msg) = appLauncher.launchAppByName(command)
                if (success) {
                    _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = "✓ $msg")
                    speaker.speak(msg)
                    return@launch
                }
            }

            // 2. Web search
            if (lower.startsWith("search google for ") || lower.startsWith("google search ") || lower.startsWith("search for ")) {
                val query = command.substringAfter("search ").removePrefix("google for ").removePrefix("for ").trim()
                appLauncher.searchWeb(query)
                val msg = "Searching Google for '$query'..."
                _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = msg)
                speaker.speak(msg)
                return@launch
            }

            // 3. Dial phone number
            if (lower.startsWith("dial ") || lower.startsWith("call ")) {
                val number = command.replace("[^0-9+]".toRegex(), "")
                if (number.isNotBlank()) {
                    appLauncher.dialPhoneNumber(number)
                    val msg = "Opening dialer for $number."
                    _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = msg)
                    speaker.speak(msg)
                    return@launch
                }
            }

            // 4. Torch / Flashlight
            if (lower.contains("torch") || lower.contains("flashlight") || lower.contains("batti")) {
                val (success, msg) = toolManager.toggleTorch()
                _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = msg)
                speaker.speak(msg)
                return@launch
            }

            // 5. Vibration / Haptics
            if (lower.contains("vibrat")) {
                toolManager.vibratePhone(200)
                val msg = "Haptic vibration pulse triggered."
                _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = msg)
                speaker.speak(msg)
                return@launch
            }

            // 6. Audio Volume
            if (lower.contains("volume") || lower.contains("awaaz")) {
                val digits = lower.replace("[^0-9]".toRegex(), "")
                val msg = if (digits.isNotBlank()) {
                    toolManager.setVolumePercent(digits.toInt())
                } else {
                    "Current media volume is ${toolManager.getVolumePercent()}%."
                }
                _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = msg)
                speaker.speak(msg)
                return@launch
            }

            // 7. Memory / Battery commands
            if (lower.contains("clean ram") || lower.contains("ram saaf") || lower.contains("boost") || lower.contains("optimize")) {
                executeTerminal("clean")
                speaker.speak("Memory optimized and reclaimed.")
                return@launch
            }

            if (lower.contains("battery") && (lower.contains("check") || lower.contains("kholo") || lower.contains("batao") || lower.contains("status"))) {
                executeTerminal("battery")
                val t = _telemetry.value
                speaker.speak("Battery level is at ${t.batteryPercent} percent, temperature ${t.batteryTempC} degrees Celsius.")
                return@launch
            }

            // 5. Missions
            if (lower.startsWith("run ") || lower.startsWith("automate ") || lower.startsWith("audit ") || lower.startsWith("diagnose ")) {
                _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = "Initiating autonomous mission: \"$command\"")
                startMission(command)
                return@launch
            }

            // 6. Direct conversational chat with Shanice
            sendChatMessage(command)
        }
    }

    fun toggleFloatingOverlay(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                speaker.speak("Please grant overlay permission so C9-Shanice can stay active on your screen.")
                return
            }
        }

        val current = _isFloatingOverlayActive.value
        if (!current) {
            val serviceIntent = Intent(context, FloatingAgentService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            _isFloatingOverlayActive.value = true
            speaker.speak("Floating screen agent is now active.")
        } else {
            val serviceIntent = Intent(context, FloatingAgentService::class.java)
            context.stopService(serviceIntent)
            _isFloatingOverlayActive.value = false
            speaker.speak("Floating screen agent deactivated.")
        }
    }

    fun refreshTelemetry() {
        viewModelScope.launch {
            val current = toolManager.getDeviceTelemetry()
            val latency = toolManager.measureNetworkLatency()
            _telemetry.value = current.copy(latencyMs = if (latency >= 0) latency else 0L)
        }
    }

    fun startMission(goal: String) {
        if (goal.isBlank() || _agentState.value == AgentState.EXECUTING_TOOL) return

        missionJob?.cancel()
        missionJob = viewModelScope.launch {
            _agentState.value = AgentState.THINKING
            executor.executeMission(goal) { mission ->
                _activeMission.value = mission
                _agentState.value = mission.status
            }
            refreshTelemetry()
        }
    }

    fun executeTerminal(command: String) {
        if (command.isBlank()) return
        viewModelScope.launch {
            val trimmed = command.trim()
            if (trimmed.equals("clear", ignoreCase = true)) {
                _terminalLines.value = listOf(
                    TerminalLine(type = LineType.SYSTEM, text = "Terminal buffer reset.")
                )
                return@launch
            }

            if (trimmed.startsWith("agent ", ignoreCase = true)) {
                val goal = trimmed.substring(6).trim()
                _terminalLines.value = _terminalLines.value + TerminalLine(type = LineType.INPUT, text = "shanice@c9-agent:~$ $command")
                _terminalLines.value = _terminalLines.value + TerminalLine(type = LineType.TOOL, text = "Dispatching autonomous mission: \"$goal\"...")
                startMission(goal)
                return@launch
            }

            val resultLines = toolManager.executeTerminalCommand(command)
            _terminalLines.value = _terminalLines.value + resultLines
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = "user", text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            // Check if user is asking to run a mission
            val lower = text.lowercase()
            if (lower.startsWith("run ") || lower.startsWith("automate ") || lower.startsWith("diagnose ") || lower.startsWith("clean ")) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = "shanice",
                    text = "Initiating autonomous mission for directive: \"$text\"."
                )
                startMission(text)
                return@launch
            }

            val history = _chatMessages.value.map { it.sender to it.text }
            val replyResult = geminiApi.chatWithShanice(text, history)
            val replyText = if (replyResult.isSuccess) {
                replyResult.getOrNull() ?: "Directive processed."
            } else {
                // Intelligent autonomous assistant fallback
                generateLocalAssistantReply(text)
            }

            _chatMessages.value = _chatMessages.value + ChatMessage(sender = "shanice", text = replyText)
            speaker.speak(replyText)
        }
    }

    private fun generateLocalAssistantReply(query: String): String {
        val q = query.lowercase()
        val t = _telemetry.value
        return when {
            q.contains("battery") -> "Device battery is currently at ${t.batteryPercent}%, running at ${t.batteryTempC}°C (${t.batteryHealth}). ${if (t.isCharging) "Power adapter active." else "Discharging on battery power."}"
            q.contains("ram") || q.contains("memory") -> "Active RAM allocation is ${t.ramUsedMb}MB of ${t.ramTotalMb}MB (${t.ramPercent}% utilization). ${t.ramAvailableMb}MB is currently available."
            q.contains("storage") -> "Internal storage status: ${t.storageUsedGb}GB used out of ${t.storageTotalGb}GB (${t.storagePercent}% capacity)."
            q.contains("stonic") || q.contains("agent") || q.contains("who are you") || q.contains("shanice") -> "I am C9-SHANICE, an autonomous mobile AI agent designed to perform system orchestration, automated diagnostics, code execution, and shell operations on Android just like Stonic AI operates on computers."
            q.contains("network") || q.contains("ping") || q.contains("wifi") -> "Network connection is ${t.networkType} [Online: ${t.isConnected}]. Last measured latency is ${t.latencyMs}ms."
            q.contains("kaise ho") || q.contains("kya haal hai") -> "Main theek hoon! C9-SHANICE system fully active hai aur aapke orders ke liye ready hai. Aap koi bhi task, diagnostics, ya code execution de sakte hain."
            else -> "Acknowledged Commander. You can ask me to run deep diagnostics, optimize memory, execute shell commands, or generate mobile automation scripts."
        }
    }

    fun updateScriptCode(code: String) {
        _scriptCode.value = code
    }

    fun setScriptLanguage(lang: String) {
        _scriptLanguage.value = lang
    }

    fun runCurrentScript() {
        val code = _scriptCode.value
        val lang = _scriptLanguage.value
        viewModelScope.launch {
            _scriptOutput.value = "Executing script in C9 sandbox..."
            val result = toolManager.evaluateScript(lang, code)
            _scriptOutput.value = result
            speaker.speak("Script execution finished.")
        }
    }

    fun loadScriptPreset(type: String) {
        when (type) {
            "battery_monitor" -> {
                _scriptLanguage.value = "Python"
                _scriptCode.value = """# Battery Telemetry Logger
# Samples thermal and power metrics
import sys

def check_power(level, temp):
    print("Logging battery sample...")
    print("Level: " + str(level) + "%")
    print("Thermal State: " + str(temp) + "C")
    if temp > 40:
        print("WARNING: Elevated thermal reading")
    else:
        print("Thermal condition is optimal")

check_power(85, 31.2)
""".trimIndent()
            }
            "memory_cleaner" -> {
                _scriptLanguage.value = "Shell"
                _scriptCode.value = """# Shell RAM Optimizer Routine
echo "Starting resident set size check..."
free_before=1450
clean
free_after=1720
echo "Reclaimed memory: 270 MB"
echo "Process exit: SUCCESS"
""".trimIndent()
            }
            "api_fetch" -> {
                _scriptLanguage.value = "JavaScript"
                _scriptCode.value = """// Mobile API Latency Probe
console.log("Connecting to edge cluster gateway...");
let latency = 45;
let packet_loss = 0.0;
console.log("Ping confirmed: " + latency + "ms");
console.log("Handshake status: 200 OK");
""".trimIndent()
            }
            "fibonacci_calc" -> {
                _scriptLanguage.value = "Python"
                _scriptCode.value = """# Fibonacci & Math Engine
print("Computing Fibonacci sequence...")
a = 0
b = 1
val = a + b
print("Fibonacci term: " + val)
print("Computation completed successfully.")
""".trimIndent()
            }
            "json_telemetry" -> {
                _scriptLanguage.value = "JavaScript"
                _scriptCode.value = """// System Hardware JSON Exporter
console.log("Compiling hardware payload...");
let cpu_cores = 8;
let ram_mb = 4096;
console.log("CPU Cores: " + cpu_cores);
console.log("RAM: " + ram_mb + " MB");
console.log("JSON Payload Verified: 200 OK");
""".trimIndent()
            }
        }
    }

    fun toggleTts() {
        val newStatus = !_isTtsEnabled.value
        _isTtsEnabled.value = newStatus
        speaker.isEnabled = newStatus
        if (newStatus) {
            speaker.speak("Voice output enabled.")
        } else {
            speaker.stop()
        }
    }

    fun setApiKey(key: String) {
        _customApiKey.value = key
        geminiApi = GeminiAgentApi(key)
    }

    fun addMemory(key: String, value: String, category: String) {
        viewModelScope.launch {
            database.memoryDao().insertMemory(
                MemoryEntity(
                    key = key,
                    value = value,
                    category = category
                )
            )
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            database.memoryDao().deleteMemory(id)
        }
    }

    fun clearMissions() {
        viewModelScope.launch {
            database.missionDao().clearAllMissions()
        }
    }

    // === WHATSAPP AI AGENT METHODS ===

    fun openDirectWhatsAppChat(number: String, message: String): Pair<Boolean, String> {
        val res = whatsAppBot.openDirectChat(number, message)
        if (res.first) speaker.speak("Opening WhatsApp chat.")
        return res
    }

    fun shareToWhatsApp(message: String) {
        whatsAppBot.shareMessageToWhatsApp(message)
    }

    fun addWhatsAppRule(keyword: String, response: String) {
        viewModelScope.launch {
            database.whatsAppDao().insertRule(
                WhatsAppRuleEntity(
                    keyword = keyword.trim(),
                    response = response.trim(),
                    isAiPowered = true,
                    isEnabled = true
                )
            )
        }
    }

    fun deleteWhatsAppRule(id: Long) {
        viewModelScope.launch {
            database.whatsAppDao().deleteRule(id)
        }
    }

    fun toggleWhatsAppRule(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            database.whatsAppDao().toggleRule(id, enabled)
        }
    }

    fun loadDefaultWhatsAppBusinessRules() {
        viewModelScope.launch {
            val defaults = listOf(
                Pair("salam", "Walaikum Assalam! C9-SHANICE WhatsApp AI Agent active hai. Main aapki kya madad kar sakta hoon?"),
                Pair("price", "Assalam-o-Alaikum! Tamam products aur packages ki competitive rates available hain. Aap konsi specific item ya service ke mutaliq janna chahte hain?"),
                Pair("order", "Order confirm karne ke liye apna Full Name, Item Name, Quantity aur Delivery Address yahan reply karein, hum foran process kareinge."),
                Pair("cod", "Cash on Delivery (COD) available hai nationwide. Iske ilawa EasyPaisa, JazzCash aur Bank Transfer bhi accept kiye jaate hain."),
                Pair("timing", "Hamara office timing Monday se Saturday 9:00 AM se 9:00 PM tak hai. WhatsApp AI Agent 24/7 active rehta hai."),
                Pair("location", "Hamara main operational hub online 24/7 active hai nationwide delivery ke sath.")
            )
            defaults.forEach { (keyword, reply) ->
                database.whatsAppDao().insertRule(
                    WhatsAppRuleEntity(
                        keyword = keyword,
                        response = reply,
                        isAiPowered = true,
                        isEnabled = true
                    )
                )
            }
        }
    }

    fun clearWhatsAppLogs() {
        viewModelScope.launch {
            database.whatsAppDao().clearLogs()
        }
    }

    fun clearWhatsAppSimulatedChat() {
        _simulatedChatMessages.value = listOf(
            WhatsAppSimChatMessage(
                sender = "bot",
                text = "Chat cleared. Send a message to test C9-SHANICE WhatsApp Bot.",
                reasoning = "Reset"
            )
        )
    }

    fun simulateWhatsAppMessage(incomingText: String, senderName: String = "Client") {
        if (incomingText.isBlank()) return
        val userMsg = WhatsAppSimChatMessage(
            sender = "client",
            text = incomingText
        )
        _simulatedChatMessages.value = _simulatedChatMessages.value + userMsg

        viewModelScope.launch {
            // Determine reasoning badge
            val lower = incomingText.lowercase(java.util.Locale.ROOT).trim()
            val matchedRule = _whatsAppRules.value.firstOrNull { it.isEnabled && lower.contains(it.keyword.lowercase(java.util.Locale.ROOT).trim()) }
            val reasoning = when {
                matchedRule != null -> "Rule: \"${matchedRule.keyword}\""
                _whatsAppCustomPrompt.value.isNotBlank() -> "Custom Business AI"
                else -> "C9-SHANICE Local AI"
            }

            val reply = whatsAppBot.generateAiReply(incomingText, senderName, _whatsAppRules.value)

            val botMsg = WhatsAppSimChatMessage(
                sender = "bot",
                text = reply,
                reasoning = reasoning
            )
            _simulatedChatMessages.value = _simulatedChatMessages.value + botMsg

            database.whatsAppDao().insertLog(
                WhatsAppMessageLog(
                    sender = senderName,
                    incomingText = incomingText,
                    replyText = reply,
                    status = "SIMULATED-REPLY"
                )
            )
            speaker.speak(reply)
        }
    }

    fun saveWhatsAppConfig(
        apiKey: String,
        phoneNumberId: String,
        webhookUrl: String,
        provider: WhatsAppApiProvider,
        persona: WhatsAppPersona,
        customPrompt: String,
        isAutoReply: Boolean
    ) {
        _whatsAppApiKey.value = apiKey
        whatsAppBot.customApiKey = apiKey

        _whatsAppPhoneNumberId.value = phoneNumberId
        whatsAppBot.phoneNumberId = phoneNumberId

        _whatsAppWebhookUrl.value = webhookUrl
        whatsAppBot.webhookUrl = webhookUrl

        _whatsAppProvider.value = provider
        whatsAppBot.apiProvider = provider

        _whatsAppPersona.value = persona
        whatsAppBot.selectedPersona = persona

        _whatsAppCustomPrompt.value = customPrompt
        whatsAppBot.customBusinessPrompt = customPrompt

        _isWhatsAppAutoReply.value = isAutoReply
        whatsAppBot.isAutoReplyActive = isAutoReply

        viewModelScope.launch {
            database.whatsAppDao().saveConfig(
                WhatsAppConfigEntity(
                    id = 1,
                    apiKey = apiKey,
                    phoneNumberId = phoneNumberId,
                    webhookUrl = webhookUrl,
                    provider = provider.id,
                    persona = persona.name,
                    customBusinessPrompt = customPrompt,
                    isAutoReplyEnabled = isAutoReply
                )
            )
            _whatsAppApiStatus.value = "Settings Saved (${provider.displayName})"
            speaker.speak("WhatsApp Agent configuration updated.")
        }
    }

    fun testWhatsAppApiConnection() {
        viewModelScope.launch {
            _whatsAppApiStatus.value = "Testing connection..."
            val (success, message) = whatsAppBot.testApiConnection()
            _whatsAppApiStatus.value = message
            speaker.speak(if (success) "Connection test successful." else "Connection test finished.")
        }
    }

    fun setWhatsAppApiKey(key: String) {
        _whatsAppApiKey.value = key
        whatsAppBot.customApiKey = key
        saveCurrentConfig()
    }

    fun setWhatsAppPersona(persona: WhatsAppPersona) {
        _whatsAppPersona.value = persona
        whatsAppBot.selectedPersona = persona
        saveCurrentConfig()
        speaker.speak("WhatsApp Persona set to ${persona.displayName}.")
    }

    fun toggleWhatsAppAutoReply(enabled: Boolean) {
        _isWhatsAppAutoReply.value = enabled
        whatsAppBot.isAutoReplyActive = enabled
        saveCurrentConfig()
        speaker.speak(if (enabled) "WhatsApp Auto-Reply active." else "WhatsApp Auto-Reply paused.")
    }

    private fun saveCurrentConfig() {
        viewModelScope.launch {
            database.whatsAppDao().saveConfig(
                WhatsAppConfigEntity(
                    id = 1,
                    apiKey = _whatsAppApiKey.value,
                    phoneNumberId = _whatsAppPhoneNumberId.value,
                    webhookUrl = _whatsAppWebhookUrl.value,
                    provider = _whatsAppProvider.value.id,
                    persona = _whatsAppPersona.value.name,
                    customBusinessPrompt = _whatsAppCustomPrompt.value,
                    isAutoReplyEnabled = _isWhatsAppAutoReply.value
                )
            )
        }
    }

    // === ANYDESK REMOTE DEVICE CONTROLLER METHODS ===

    fun startRemoteHostMode() {
        remoteDevice.startHostMode()
    }

    fun connectRemoteDevice(targetIp: String, port: Int = 8765, inviteCode: String = "", pin: String = "") {
        remoteDevice.connectAsController(targetIp, port, inviteCode, pin)
    }

    fun startRemoteSandbox(targetDeviceName: String = "Samsung Galaxy S24 Ultra") {
        remoteDevice.startSandboxConnection(targetDeviceName)
    }

    fun regenerateRemoteInviteCode() {
        remoteDevice.regenerateCodeAndPin()
    }

    fun refreshNetworkInfo() {
        remoteDevice.refreshNetworkInfo()
    }

    fun sendRemoteAppLaunch(packageName: String, appName: String) {
        remoteDevice.sendRemoteAppLaunch(packageName, appName)
    }

    fun sendRemoteVibrateAlert() {
        remoteDevice.sendRemoteVibrationAlert()
    }

    fun sendRemoteTorchToggle() {
        remoteDevice.sendRemoteTorchToggle()
    }

    fun sendRemoteClipboard(text: String) {
        remoteDevice.sendRemoteClipboard(text)
    }

    fun sendRemoteFile(fileName: String, fileType: String, content: String) {
        remoteDevice.sendFileToRemote(fileName, fileType, content)
    }

    fun sendRemoteDirective(directive: String) {
        remoteDevice.sendRemoteDirective(directive)
    }

    fun simulateRemoteNavKey(key: String) {
        remoteDevice.simulateRemoteKey(key)
    }

    fun terminateRemoteSession() {
        remoteDevice.terminateSession()
    }

    override fun onCleared() {
        super.onCleared()
        remoteDevice.terminateSession()
        speaker.shutdown()
    }
}
