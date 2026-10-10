package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AgentGuidelinesDialog
import com.example.ui.screens.AgentHudScreen
import com.example.ui.screens.AppLauncherScreen
import com.example.ui.screens.DeviceTelemetryScreen
import com.example.ui.screens.MemoryKnowledgeScreen
import com.example.ui.screens.RemoteControlScreen
import com.example.ui.screens.ScriptStudioScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.screens.WhatsAppAgentScreen
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.AgentViewModel
import java.util.Locale

enum class AgentTab(val title: String, val icon: ImageVector) {
    HUD("Mission", Icons.Default.Psychology),
    REMOTE("AnyLink", Icons.Default.Devices),
    WHATSAPP("WhatsApp", Icons.AutoMirrored.Filled.Chat),
    TASKS("Tasks", Icons.Default.Apps),
    TERMINAL("Terminal", Icons.Default.Terminal),
    TELEMETRY("Sensors", Icons.Default.Memory),
    STUDIO("Script", Icons.Default.Code),
    MEMORY("Memory", Icons.Default.Storage)
}

class MainActivity : ComponentActivity() {
    private val splashVisibleState = mutableStateOf(true)
    private val guidelinesVisibleState = mutableStateOf(true)
    private var isLaunchingExternalIntent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashVisibleState.value = true
        guidelinesVisibleState.value = true
        setContent {
            MyApplicationTheme {
                MainAgentApp(
                    splashVisibleState = splashVisibleState,
                    guidelinesVisibleState = guidelinesVisibleState,
                    onLaunchingExternal = { isLaunchingExternalIntent = it }
                )
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        // Har bar jab app background / home ya recent apps se reopen ho, splash screen aur guidelines dialog aye
        if (!isLaunchingExternalIntent) {
            splashVisibleState.value = true
            guidelinesVisibleState.value = true
        }
        isLaunchingExternalIntent = false
    }
}

@Composable
fun MainAgentApp(
    splashVisibleState: MutableState<Boolean> = remember { mutableStateOf(true) },
    guidelinesVisibleState: MutableState<Boolean> = remember { mutableStateOf(true) },
    onLaunchingExternal: (Boolean) -> Unit = {}
) {
    val viewModel: AgentViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(AgentTab.HUD) }
    val context = LocalContext.current
    val activity = context as? Activity

    Crossfade(
        targetState = splashVisibleState.value,
        label = "splash_screen_transition"
    ) { isSplash ->
        if (isSplash) {
            SplashScreen(
                onFinish = { splashVisibleState.value = false }
            )
        } else {
            // Dialog box har bar app open krny py home screen k uper show ho
            if (guidelinesVisibleState.value) {
                AgentGuidelinesDialog(
                    onDismiss = { guidelinesVisibleState.value = false }
                )
            }

            val globalSpeechLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                onLaunchingExternal(false)
                if (result.resultCode == Activity.RESULT_OK) {
                    val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                    if (!spoken.isNullOrBlank()) {
                        viewModel.processVoiceCommand(spoken)
                    }
                }
            }

            // Ensure back button returns to HUD if currently in a subscreen, or cleanly exits so next open triggers splash & guidelines
            BackHandler(enabled = true) {
                if (selectedTab != AgentTab.HUD) {
                    selectedTab = AgentTab.HUD
                } else {
                    splashVisibleState.value = true
                    guidelinesVisibleState.value = true
                    activity?.finish()
                }
            }

            Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            // Always-on floating voice trigger button
            FloatingActionButton(
                onClick = {
                    onLaunchingExternal(true)
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "C9-SHANICE: Speak your command (e.g. Open YouTube, Clean RAM)...")
                    }
                    try {
                        globalSpeechLauncher.launch(intent)
                    } catch (e: Exception) {
                        onLaunchingExternal(false)
                        // speech recognition unavailable
                    }
                },
                containerColor = NeonCyan,
                contentColor = Color(0xFF001A22),
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-10).dp)
                    .border(2.dp, Color(0xFF70F5FF), CircleShape)
                    .testTag("global_floating_voice_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Quick Voice Command",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                AgentTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF001A22),
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberBlack)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AgentTab.HUD -> AgentHudScreen(
                    viewModel = viewModel,
                    onOpenWhatsApp = { selectedTab = AgentTab.WHATSAPP },
                    onOpenRemote = { selectedTab = AgentTab.REMOTE },
                    onReplaySplash = { splashVisibleState.value = true },
                    onOpenGuidelines = { guidelinesVisibleState.value = true }
                )
                AgentTab.REMOTE -> RemoteControlScreen(viewModel = viewModel)
                AgentTab.WHATSAPP -> WhatsAppAgentScreen(viewModel = viewModel)
                AgentTab.TASKS -> AppLauncherScreen(viewModel = viewModel)
                AgentTab.TERMINAL -> TerminalScreen(viewModel = viewModel)
                AgentTab.TELEMETRY -> DeviceTelemetryScreen(viewModel = viewModel)
                AgentTab.STUDIO -> ScriptStudioScreen(viewModel = viewModel)
                AgentTab.MEMORY -> MemoryKnowledgeScreen(viewModel = viewModel)
            }
        }
    }
        }
    }
}
