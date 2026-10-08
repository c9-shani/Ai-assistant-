package com.example.data.model

enum class AgentState {
    IDLE,
    THINKING,
    EXECUTING_TOOL,
    SYNTHESIZING,
    COMPLETE,
    ERROR
}

enum class StepStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED
}

enum class ToolType {
    DEVICE_DIAGNOSTICS,
    TERMINAL_EXEC,
    MEMORY_RETRIEVAL,
    MEMORY_STORE,
    NETWORK_PROBE,
    CLIPBOARD_ACTION,
    SCRIPT_RUNNER,
    SPEECH_SYNTHESIS,
    WEB_SYNTHESIS
}

data class MissionStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val toolType: ToolType,
    val toolInput: String = "",
    val output: String = "",
    val status: StepStatus = StepStatus.PENDING,
    val executionTimeMs: Long = 0
)

data class ActiveMission(
    val id: String = java.util.UUID.randomUUID().toString(),
    val goalPrompt: String,
    val steps: List<MissionStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val status: AgentState = AgentState.IDLE,
    val finalReport: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

data class DeviceTelemetry(
    val ramTotalMb: Long = 0,
    val ramUsedMb: Long = 0,
    val ramAvailableMb: Long = 0,
    val ramPercent: Int = 0,
    val batteryPercent: Int = 0,
    val batteryTempC: Float = 0f,
    val isCharging: Boolean = false,
    val batteryHealth: String = "Good",
    val storageTotalGb: Double = 0.0,
    val storageUsedGb: Double = 0.0,
    val storagePercent: Int = 0,
    val networkType: String = "Unknown",
    val isConnected: Boolean = false,
    val latencyMs: Long = 0,
    val deviceModel: String = "Android Device",
    val androidVersion: String = "Android",
    val uptimeHours: Double = 0.0,
    val processorCores: Int = 8
)

enum class LineType {
    INPUT,
    OUTPUT,
    ERROR,
    SYSTEM,
    TOOL,
    AGENT
}

data class TerminalLine(
    val id: Long = System.nanoTime(),
    val type: LineType,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class WorkflowPreset(
    val id: String,
    val title: String,
    val description: String,
    val prompt: String,
    val tags: List<String>
)
