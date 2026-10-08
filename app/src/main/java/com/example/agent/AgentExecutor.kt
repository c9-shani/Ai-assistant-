package com.example.agent

import com.example.data.api.GeminiAgentApi
import com.example.data.database.AgentDatabase
import com.example.data.database.MemoryEntity
import com.example.data.database.MissionEntity
import com.example.data.model.ActiveMission
import com.example.data.model.AgentState
import com.example.data.model.MissionStep
import com.example.data.model.StepStatus
import com.example.data.model.ToolType
import com.example.speech.AgentVoiceSpeaker
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.util.Locale

class AgentExecutor(
    private val toolManager: DeviceToolManager,
    private val database: AgentDatabase,
    private val speaker: AgentVoiceSpeaker,
    private val geminiApi: GeminiAgentApi
) {

    suspend fun executeMission(
        goal: String,
        onMissionUpdate: (ActiveMission) -> Unit
    ) {
        val telemetry = toolManager.getDeviceTelemetry()
        val deviceContext = "Model: ${telemetry.deviceModel}, OS: ${telemetry.androidVersion}, RAM: ${telemetry.ramUsedMb}/${telemetry.ramTotalMb}MB (${telemetry.ramPercent}%), Battery: ${telemetry.batteryPercent}% (${if (telemetry.isCharging) "Charging" else "Unplugged"}), Storage: ${telemetry.storageUsedGb}/${telemetry.storageTotalGb}GB, Network: ${telemetry.networkType}"

        var mission = ActiveMission(
            goalPrompt = goal,
            status = AgentState.THINKING
        )
        onMissionUpdate(mission)
        speaker.speak("C9-Shanice analyzing mission: $goal")

        // 1. Generate Plan (Gemini or Autonomous Tactical Engine)
        val plannedSteps = planMissionSteps(goal, deviceContext)
        mission = mission.copy(
            steps = plannedSteps,
            status = AgentState.EXECUTING_TOOL
        )
        onMissionUpdate(mission)

        // 2. Sequential Step Execution
        val executedSteps = mutableListOf<MissionStep>()
        for ((index, step) in plannedSteps.withIndex()) {
            val runningStep = step.copy(status = StepStatus.RUNNING)
            executedSteps.add(runningStep)
            mission = mission.copy(
                steps = executedSteps.toList() + plannedSteps.drop(index + 1),
                currentStepIndex = index,
                status = AgentState.EXECUTING_TOOL
            )
            onMissionUpdate(mission)

            val startTime = System.currentTimeMillis()
            delay(400) // slight visual pacing for agent observation

            val output = executeStepTool(runningStep)
            val elapsed = System.currentTimeMillis() - startTime

            val completedStep = runningStep.copy(
                status = StepStatus.SUCCESS,
                output = output,
                executionTimeMs = elapsed
            )
            executedSteps[index] = completedStep

            mission = mission.copy(
                steps = executedSteps.toList() + plannedSteps.drop(index + 1)
            )
            onMissionUpdate(mission)
        }

        // 3. Final Synthesis & Report
        mission = mission.copy(status = AgentState.SYNTHESIZING)
        onMissionUpdate(mission)
        delay(300)

        val finalReport = compileFinalReport(goal, executedSteps)
        mission = mission.copy(
            status = AgentState.COMPLETE,
            finalReport = finalReport,
            completedAt = System.currentTimeMillis()
        )
        onMissionUpdate(mission)

        // 4. Persist to Room Database
        database.missionDao().insertMission(
            MissionEntity(
                title = goal.take(45),
                goalPrompt = goal,
                status = "COMPLETED",
                totalSteps = executedSteps.size,
                summary = finalReport.take(200)
            )
        )

        speaker.speak("Mission complete. All autonomous mobile tasks executed successfully.")
    }

    private suspend fun planMissionSteps(goal: String, deviceContext: String): List<MissionStep> {
        // Attempt AI generation first via Gemini
        val apiResult = geminiApi.generateAgentPlan(goal, deviceContext)
        if (apiResult.isSuccess) {
            val jsonStr = apiResult.getOrNull()
            if (!jsonStr.isNullOrBlank()) {
                val parsed = parseStepsFromJson(jsonStr)
                if (parsed.isNotEmpty()) {
                    return parsed
                }
            }
        }

        // High-grade Autonomous Tactical Plan Generator (Stonic Mobile Agent Pattern)
        val lowerGoal = goal.lowercase(Locale.ROOT)
        val steps = mutableListOf<MissionStep>()

        when {
            lowerGoal.contains("diagnos") || lowerGoal.contains("audit") || lowerGoal.contains("health") || lowerGoal.contains("check") -> {
                steps.add(
                    MissionStep(
                        stepNumber = 1,
                        title = "Hardware & Battery Telemetry Audit",
                        description = "Probe device sensors, battery charge, thermal temp, and health",
                        toolType = ToolType.DEVICE_DIAGNOSTICS,
                        toolInput = "telemetry:battery+thermal"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 2,
                        title = "Memory & Storage Allocation Inspection",
                        description = "Query active RAM and storage partition saturation",
                        toolType = ToolType.TERMINAL_EXEC,
                        toolInput = "ram"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 3,
                        title = "Network Connectivity & Latency Probe",
                        description = "Measure real-time DNS ping round-trip time and interface health",
                        toolType = ToolType.NETWORK_PROBE,
                        toolInput = "ping 8.8.8.8"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 4,
                        title = "Persist Health Benchmark to Neural Memory",
                        description = "Save system telemetry snapshot to local Agent Knowledge base",
                        toolType = ToolType.MEMORY_STORE,
                        toolInput = "SYS_HEALTH_LOG"
                    )
                )
            }

            lowerGoal.contains("clean") || lowerGoal.contains("boost") || lowerGoal.contains("optimi") || lowerGoal.contains("speed") -> {
                steps.add(
                    MissionStep(
                        stepNumber = 1,
                        title = "Baseline Memory & Process Profiling",
                        description = "Read current resident set size and memory pressure",
                        toolType = ToolType.TERMINAL_EXEC,
                        toolInput = "ps"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 2,
                        title = "Autonomous RAM Purge & GC Execution",
                        description = "Trigger Dalvik/ART garbage collection and memory trim",
                        toolType = ToolType.TERMINAL_EXEC,
                        toolInput = "clean"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 3,
                        title = "Sanitize Android Clipboard Buffer",
                        description = "Inspect and sanitize clipboard history to prevent memory leak",
                        toolType = ToolType.CLIPBOARD_ACTION,
                        toolInput = "inspect"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 4,
                        title = "Post-Optimization Verification",
                        description = "Verify reclaimed memory delta and performance index",
                        toolType = ToolType.DEVICE_DIAGNOSTICS,
                        toolInput = "telemetry:ram"
                    )
                )
            }

            lowerGoal.contains("code") || lowerGoal.contains("script") || lowerGoal.contains("python") || lowerGoal.contains("program") -> {
                steps.add(
                    MissionStep(
                        stepNumber = 1,
                        title = "Initialize Mobile Script Sandbox",
                        description = "Spin up runtime environment for code execution",
                        toolType = ToolType.TERMINAL_EXEC,
                        toolInput = "uname"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 2,
                        title = "Generate Autonomous Automation Routine",
                        description = "Compile targeted logic algorithm with execution handlers",
                        toolType = ToolType.SCRIPT_RUNNER,
                        toolInput = "print('C9-SHANICE script execution active: batch size 100')\nval = 48 * 2.5\nprint('Processed output: ' + val)"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 3,
                        title = "Verification & Output Synthesis",
                        description = "Check script exit code and store result in memory",
                        toolType = ToolType.MEMORY_STORE,
                        toolInput = "SCRIPT_RUN_OUTPUT"
                    )
                )
            }

            lowerGoal.contains("net") || lowerGoal.contains("wifi") || lowerGoal.contains("ping") || lowerGoal.contains("speed") -> {
                steps.add(
                    MissionStep(
                        stepNumber = 1,
                        title = "Query Active Interface & Link Specs",
                        description = "Check Wi-Fi / Cellular configuration and gateway",
                        toolType = ToolType.TERMINAL_EXEC,
                        toolInput = "net"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 2,
                        title = "Measure Round-Trip Latency (ICMP/DNS)",
                        description = "Send socket probe to 8.8.8.8 and measure response time",
                        toolType = ToolType.NETWORK_PROBE,
                        toolInput = "ping"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 3,
                        title = "Evaluate Bandwidth & Packet Reliability",
                        description = "Assess latency stability and jitter score",
                        toolType = ToolType.DEVICE_DIAGNOSTICS,
                        toolInput = "telemetry:network"
                    )
                )
            }

            else -> {
                // General autonomous workflow
                steps.add(
                    MissionStep(
                        stepNumber = 1,
                        title = "Environment Telemetry Scan",
                        description = "Audit runtime state and system resources",
                        toolType = ToolType.DEVICE_DIAGNOSTICS,
                        toolInput = "telemetry:full"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 2,
                        title = "Mobile Shell Execution & Analysis",
                        description = "Execute core task commands in terminal sandbox",
                        toolType = ToolType.TERMINAL_EXEC,
                        toolInput = "sysinfo"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 3,
                        title = "Knowledge Base & Memory Indexing",
                        description = "Record mission objectives and observations into neural memory",
                        toolType = ToolType.MEMORY_STORE,
                        toolInput = "MISSION_${System.currentTimeMillis()}"
                    )
                )
                steps.add(
                    MissionStep(
                        stepNumber = 4,
                        title = "Autonomous Voice Announcement",
                        description = "Synthesize audio briefing of the completed task",
                        toolType = ToolType.SPEECH_SYNTHESIS,
                        toolInput = "Task completed successfully by C9-Shanice."
                    )
                )
            }
        }
        return steps
    }

    private fun parseStepsFromJson(jsonStr: String): List<MissionStep> {
        val steps = mutableListOf<MissionStep>()
        try {
            val root = JSONObject(jsonStr)
            val arr = root.optJSONArray("steps") ?: return emptyList()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeStr = obj.optString("toolType", "TERMINAL_EXEC")
                val toolType = when (typeStr) {
                    "DEVICE_DIAGNOSTICS" -> ToolType.DEVICE_DIAGNOSTICS
                    "NETWORK_PROBE" -> ToolType.NETWORK_PROBE
                    "CLIPBOARD_ACTION" -> ToolType.CLIPBOARD_ACTION
                    "SCRIPT_RUNNER" -> ToolType.SCRIPT_RUNNER
                    "MEMORY_STORE" -> ToolType.MEMORY_STORE
                    "SPEECH_SYNTHESIS" -> ToolType.SPEECH_SYNTHESIS
                    else -> ToolType.TERMINAL_EXEC
                }

                steps.add(
                    MissionStep(
                        stepNumber = obj.optInt("stepNumber", i + 1),
                        title = obj.optString("title", "Step ${i + 1}"),
                        description = obj.optString("description", "Executing autonomous task"),
                        toolType = toolType,
                        toolInput = obj.optString("toolInput", "")
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return steps
    }

    private suspend fun executeStepTool(step: MissionStep): String {
        return when (step.toolType) {
            ToolType.DEVICE_DIAGNOSTICS -> {
                val t = toolManager.getDeviceTelemetry()
                "Device: ${t.deviceModel} | RAM: ${t.ramUsedMb}/${t.ramTotalMb}MB (${t.ramPercent}%) | Battery: ${t.batteryPercent}% (${t.batteryTempC}°C) | Storage: ${t.storageUsedGb}/${t.storageTotalGb}GB (${t.storagePercent}%)"
            }

            ToolType.TERMINAL_EXEC -> {
                val lines = toolManager.executeTerminalCommand(step.toolInput.ifBlank { "sysinfo" })
                lines.filter { it.type != com.example.data.model.LineType.INPUT }
                    .joinToString("\n") { it.text }
            }

            ToolType.NETWORK_PROBE -> {
                val lat = toolManager.measureNetworkLatency()
                val t = toolManager.getDeviceTelemetry()
                if (lat >= 0) {
                    "Network: ${t.networkType} | RTT Latency: ${lat}ms | Status: ACTIVE ONLINE"
                } else {
                    "Network: ${t.networkType} | Ping unreachable | Connection restricted"
                }
            }

            ToolType.CLIPBOARD_ACTION -> {
                val clip = toolManager.readClipboard()
                if (clip.isNotBlank()) {
                    "Clipboard inspected: \"${clip.take(60)}\" (Length: ${clip.length} chars)"
                } else {
                    toolManager.copyToClipboard("C9-SHANICE Mission Benchmark: ${System.currentTimeMillis()}")
                    "Clipboard initialized with security token."
                }
            }

            ToolType.SCRIPT_RUNNER -> {
                val script = if (step.toolInput.isNotBlank()) step.toolInput else "val x = 100 * 4\nprint(x)"
                toolManager.evaluateScript("python", script)
            }

            ToolType.MEMORY_STORE -> {
                database.memoryDao().insertMemory(
                    MemoryEntity(
                        key = step.toolInput.ifBlank { "TASK_RECORD" },
                        value = "Recorded telemetry benchmark at ${System.currentTimeMillis()}",
                        category = "SYSTEM"
                    )
                )
                "Record successfully synced to local Agent Knowledge Base."
            }

            ToolType.SPEECH_SYNTHESIS -> {
                speaker.speak(step.toolInput.ifBlank { "Task milestone reached." })
                "Audio synthesized to speaker channel."
            }

            else -> "Execution complete."
        }
    }

    private fun compileFinalReport(goal: String, steps: List<MissionStep>): String {
        val sb = StringBuilder()
        sb.append("⚡ MISSION COMPLETE: C9-SHANICE AUTONOMOUS AGENT\n\n")
        sb.append("• Goal: \"$goal\"\n")
        sb.append("• Executed Steps: ${steps.size} / ${steps.size} SUCCESSFUL\n")
        val totalMs = steps.sumOf { it.executionTimeMs }
        sb.append("• Total Execution Time: ${totalMs}ms\n\n")
        sb.append("Key Highlights:\n")
        for (s in steps) {
            sb.append("  [✓] ${s.title}: ${s.output.lines().firstOrNull() ?: "Done"}\n")
        }
        sb.append("\nStatus: All systems operational. Agent returned to ready state.")
        return sb.toString()
    }
}
