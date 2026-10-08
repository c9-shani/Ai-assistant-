package com.example.data.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAgentApi(private val customApiKey: String? = null) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun resolveKey(): String {
        val custom = customApiKey?.trim()
        if (!custom.isNullOrEmpty()) return custom
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
    }

    suspend fun generateAgentPlan(
        goal: String,
        deviceContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalStateException("No valid GEMINI_API_KEY found"))
        }

        try {
            val systemInstruction = """
                You are C9-SHANICE, an advanced autonomous Cybernetic AI Agent for Android mobile, functioning like Stonic AI / Computer Agent on desktop.
                Your task is to take a high-level user goal, inspect the mobile device context, and execute an autonomous plan.
                Always respond in clean JSON format matching this schema:
                {
                   "summary": "Short 1-2 sentence overview of the tactical plan",
                   "steps": [
                      {
                         "stepNumber": 1,
                         "title": "Title of step",
                         "description": "What is being executed",
                         "toolType": "DEVICE_DIAGNOSTICS" | "TERMINAL_EXEC" | "NETWORK_PROBE" | "CLIPBOARD_ACTION" | "SCRIPT_RUNNER" | "MEMORY_STORE" | "SPEECH_SYNTHESIS",
                         "toolInput": "Argument or shell command or script or text"
                      }
                   ],
                   "conclusion": "Final recommendation or action summary"
                }
            """.trimIndent()

            val promptText = "User Goal: $goal\n\nCurrent Mobile Device Telemetry:\n$deviceContext\n\nGenerate the autonomous step-by-step execution plan."

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()
                partsArray.put(JSONObject().put("text", promptText))
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                val sysObj = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemInstruction))
                sysObj.put("parts", sysParts)
                put("systemInstruction", sysObj)

                val genConfig = JSONObject()
                genConfig.put("responseMimeType", "application/json")
                genConfig.put("temperature", 0.3)
                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "HTTP ${response.code}"
                return@withContext Result.failure(Exception("Gemini API Error: $errBody"))
            }

            val respString = response.body?.string() ?: ""
            val respJson = JSONObject(respString)
            val candidates = respJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chatWithShanice(
        userMessage: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalStateException("No valid GEMINI_API_KEY found"))
        }

        try {
            val systemInstruction = """
                You are C9-SHANICE, an elite autonomous Mobile AI Agent operating on Android.
                You are inspired by Stonic AI and computer-use autonomous agents.
                You can automate tasks, execute terminal commands, run code scripts, monitor device battery, ram, and storage, and execute complex workflows.
                You speak with intelligence, precision, high confidence, and a subtle cybernetic touch.
                Support English, Urdu, and Roman Urdu fluently depending on how the user addresses you.
                Keep responses concise, helpful, and action-oriented.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray()

                // Add past turns
                for (turn in conversationHistory.takeLast(6)) {
                    val role = if (turn.first == "user") "user" else "model"
                    val turnObj = JSONObject()
                    turnObj.put("role", role)
                    val turnParts = JSONArray()
                    turnParts.put(JSONObject().put("text", turn.second))
                    turnObj.put("parts", turnParts)
                    contentsArray.put(turnObj)
                }

                // Current message
                val curObj = JSONObject()
                curObj.put("role", "user")
                val curParts = JSONArray()
                curParts.put(JSONObject().put("text", userMessage))
                curObj.put("parts", curParts)
                contentsArray.put(curObj)

                put("contents", contentsArray)

                val sysObj = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemInstruction))
                sysObj.put("parts", sysParts)
                put("systemInstruction", sysObj)

                val genConfig = JSONObject()
                genConfig.put("temperature", 0.7)
                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "HTTP ${response.code}"
                return@withContext Result.failure(Exception("Gemini API Error: $errBody"))
            }

            val respString = response.body?.string() ?: ""
            val respJson = JSONObject(respString)
            val candidates = respJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: "Acknowledged, Commander."

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
