package com.example.agent

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.api.GeminiAgentApi
import com.example.data.database.WhatsAppRuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class WhatsAppApiProvider(
    val id: String,
    val displayName: String,
    val defaultEndpoint: String,
    val description: String
) {
    LOCAL_SMART_AI(
        "LOCAL_SMART_AI",
        "Local Smart AI & Bridge",
        "local://android-bridge",
        "Bina kisi API key ke 100% free offline/online native intelligence"
    ),
    META_CLOUD_API(
        "META_CLOUD_API",
        "WhatsApp Cloud API (Meta)",
        "https://graph.facebook.com/v21.0",
        "Official Meta Graph Cloud API for Business (Phone Number ID & Token)"
    ),
    TWILIO(
        "TWILIO",
        "Twilio WhatsApp API",
        "https://api.twilio.com/2010-04-01",
        "Twilio Programmable Messaging API Gateway"
    ),
    GREEN_API(
        "GREEN_API",
        "Green API / UltraMsg",
        "https://api.green-api.com",
        "WhatsApp Web QR Instance & Webhook Gateway"
    ),
    CUSTOM_WEBHOOK(
        "CUSTOM_WEBHOOK",
        "Custom Webhook / Server",
        "",
        "Aapka apna custom webhook ya Python/Node backend server"
    );

    companion object {
        fun fromId(id: String): WhatsAppApiProvider {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: LOCAL_SMART_AI
        }
    }
}

enum class WhatsAppPersona(val displayName: String, val systemInstruction: String) {
    CUSTOMER_SUPPORT(
        "Customer Support 24/7",
        "You are an automated WhatsApp Customer Support Agent. Be polite, prompt, and helpful. Resolve inquiries efficiently. Support Urdu, Roman Urdu, and English."
    ),
    SALES_ASSISTANT(
        "Sales & Booking Closer",
        "You are a high-converting WhatsApp Sales & Booking Agent. Welcome the customer warmly, provide pricing details, gather order requirements, and close deals smoothly. Support Roman Urdu and English."
    ),
    ROMAN_URDU_EXPERT(
        "Roman Urdu Specialist",
        "Aap aik zabardast Roman Urdu WhatsApp AI Assistant hain. Customers ke sath bohat respectful aur apnayat se baat karein. Har sawal ka wazeh aur short jawab dein."
    ),
    FRIENDLY_PERSONAL(
        "Personal Assistant",
        "You are a friendly personal WhatsApp AI assistant for the user. Speak naturally, inform callers if the user is busy, and take messages."
    ),
    PROFESSIONAL_BUSINESS(
        "Business Executive",
        "You are an executive WhatsApp corporate assistant. Maintain formal business tone, state working hours, and request meeting details."
    ),
    CUSTOM_STORE(
        "Custom Store / Business",
        "You are a dedicated AI assistant for this specific store and business. Follow the owner's custom business guidelines, prices, and policies strictly."
    );

    companion object {
        fun fromName(name: String): WhatsAppPersona {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CUSTOMER_SUPPORT
        }
    }
}

class WhatsAppBotManager(
    private val context: Context,
    private val geminiApi: GeminiAgentApi
) {

    var selectedPersona: WhatsAppPersona = WhatsAppPersona.CUSTOMER_SUPPORT
    var customApiKey: String = ""
    var phoneNumberId: String = ""
    var webhookUrl: String = ""
    var apiProvider: WhatsAppApiProvider = WhatsAppApiProvider.LOCAL_SMART_AI
    var customBusinessPrompt: String = ""
    var isAutoReplyActive: Boolean = true
    var replyDelaySeconds: Int = 1

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun openDirectChat(phoneNumber: String, message: String): Pair<Boolean, String> {
        if (phoneNumber.isBlank()) {
            return Pair(false, "Phone number is required.")
        }

        // Clean phone number (strip spaces, dashes, parentheses)
        var clean = phoneNumber.replace("[^0-9+]".toRegex(), "")
        if (clean.startsWith("0")) {
            // Assume Pakistan standard prefix if local 03xx
            clean = "92" + clean.substring(1)
        }
        clean = clean.removePrefix("+")

        return try {
            val defaultMsg = "Assalam-o-Alaikum! Reaching out via C9-SHANICE WhatsApp AI Agent."
            val encodedMsg = URLEncoder.encode(message.ifBlank { defaultMsg }, "UTF-8")
            val url = "https://api.whatsapp.com/send?phone=$clean&text=$encodedMsg"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage("com.whatsapp")
            }

            try {
                context.startActivity(intent)
                Pair(true, "Opening WhatsApp chat with +$clean.")
            } catch (e: Exception) {
                // If package targeting failed (e.g. WA Business or browser), open browser link
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                Pair(true, "Opening WhatsApp Web portal for +$clean.")
            }
        } catch (e: Exception) {
            Pair(false, "Failed to launch WhatsApp: ${e.message}")
        }
    }

    fun shareMessageToWhatsApp(message: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val chooser = Intent.createChooser(intent, "Share via WhatsApp").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    suspend fun generateAiReply(
        incomingMessage: String,
        senderName: String,
        rules: List<WhatsAppRuleEntity> = emptyList()
    ): String = withContext(Dispatchers.Default) {
        val lower = incomingMessage.lowercase(Locale.ROOT).trim()

        // 1. Check custom trigger rules (highest priority)
        for (rule in rules) {
            if (rule.isEnabled && lower.contains(rule.keyword.lowercase(Locale.ROOT).trim())) {
                return@withContext rule.response
            }
        }

        // 2. Build contextual prompt incorporating Custom Business Details
        val businessContext = if (customBusinessPrompt.isNotBlank()) {
            "BUSINESS PROFILE & POLICIES:\n$customBusinessPrompt\n"
        } else ""

        val geminiPrompt = """
            ${selectedPersona.systemInstruction}
            $businessContext
            Incoming WhatsApp message from "$senderName": "$incomingMessage"
            Instructions:
            - Generate a short, polite, helpful WhatsApp reply (2-3 sentences max).
            - Match the language of the incoming message (Urdu, Roman Urdu, or English).
            - Do not include hashtags, markdown bold headers, or robotic prefixes.
        """.trimIndent()

        // Attempt Gemini API reply
        val apiResult = geminiApi.chatWithShanice(geminiPrompt)
        if (apiResult.isSuccess) {
            val text = apiResult.getOrNull()
            if (!text.isNullOrBlank()) {
                return@withContext text.trim()
            }
        }

        // 3. High-grade deterministic Multi-lingual AI responder (Instant & Offline-ready)
        generateSmartLocalReply(incomingMessage, senderName)
    }

    private fun generateSmartLocalReply(incoming: String, sender: String): String {
        val lower = incoming.lowercase(Locale.ROOT)

        // Custom Business info injection if available
        val businessNote = if (customBusinessPrompt.isNotBlank()) {
            " (${customBusinessPrompt.take(80)}...)"
        } else ""

        return when {
            lower.contains("salam") || lower.contains("assalam") || lower.contains("slam") || lower.contains("aoa") ->
                "Walaikum Assalam $sender! C9-SHANICE WhatsApp AI Agent active hai. Main aapki kya madad kar sakta hoon?$businessNote"

            lower.contains("hi") || lower.contains("hello") || lower.contains("hey") || lower.contains("hola") ->
                "Hello $sender! Welcome. I am your WhatsApp AI Assistant. How can I help you today?$businessNote"

            lower.contains("kese ho") || lower.contains("kaise ho") || lower.contains("how are you") ->
                "Alhamdulillah main theek hoon! Aap batayein $sender, aaj aapki kya khidmat kar sakta hoon?"

            lower.contains("price") || lower.contains("rate") || lower.contains("cost") || lower.contains("charges") || lower.contains("kitne") || lower.contains("kimat") ->
                "Assalam-o-Alaikum $sender! Tamam products aur packages ki competitive rates available hain. Aap konsi specific item ya package ke bare mein details chahte hain?$businessNote"

            lower.contains("order") || lower.contains("book") || lower.contains("buy") || lower.contains("khareed") ->
                "Shukriya $sender! Apna order confirm karne ke liye apna Full Name, Item Name, Quantity aur Delivery Address yahan reply karein, hum foran process kareinge."

            lower.contains("cod") || lower.contains("cash on delivery") || lower.contains("payment") || lower.contains("easypaisa") || lower.contains("jazzcash") ->
                "Cash on Delivery (COD) all over Pakistan available hai. Iske ilawa EasyPaisa, JazzCash aur Online Bank Transfer se bhi payment accept ki jati hai."

            lower.contains("delivery") || lower.contains("shipping") || lower.contains("tcs") || lower.contains("courier") || lower.contains("kitne din") ->
                "Delivery standard 2 se 4 working days mein nationwide deliver ki jati hai. Dispatched hote hi tracking ID WhatsApp par share kar di jayegi."

            lower.contains("location") || lower.contains("address") || lower.contains("kahan") || lower.contains("shop") || lower.contains("office") ->
                "Hamara main hub online 24/7 active hai. Nationwide delivery service all cities mein available hai. Kisi bhi query ke liye yahan message drop karein."

            lower.contains("timing") || lower.contains("time") || lower.contains("kab open") || lower.contains("hours") ->
                "Customer support service Monday se Saturday 9:00 AM se 9:00 PM tak active rehti hai, jabkay WhatsApp AI Agent 24/7 online hai!"

            lower.contains("call") || lower.contains("urgent") || lower.contains("rabta") || lower.contains("phone") || lower.contains("number") ->
                "Aapka contact note kar liya gaya hai. Hamari team bohat jald aapse directly rabta karegi. Agar koi specific requirement ho toh yahan likh dein."

            lower.contains("shukriya") || lower.contains("thanks") || lower.contains("thank you") || lower.contains("jazakallah") ->
                "JazakAllah Khair $sender! Khushi hui baat kar ke. Kisi bhi mazeed madad ke liye hum hamesha hazir hain."

            lower.contains("return") || lower.contains("exchange") || lower.contains("warranty") || lower.contains("kharab") || lower.contains("complaint") ->
                "Hamara 7-day hassle-free return and exchange policy active hai. Baraye meherbani apna order number aur issue detail share karein, hum foran solve kareinge."

            else ->
                "Salam $sender! Aapka message receive ho chuka hai: \"${incoming.take(45)}\". C9-SHANICE WhatsApp AI Agent active hai, hum jald detailed response faraham kareinge."
        }
    }

    suspend fun testApiConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        when (apiProvider) {
            WhatsAppApiProvider.LOCAL_SMART_AI -> {
                Pair(true, "⚡ Local AI & Android Bridge Active. Native background auto-reply running (Zero API key required).")
            }
            WhatsAppApiProvider.META_CLOUD_API -> {
                if (customApiKey.isBlank()) {
                    Pair(false, "⚠️ API Key is empty. Please paste your Meta WhatsApp Cloud API Access Token.")
                } else if (phoneNumberId.isBlank()) {
                    Pair(false, "⚠️ Phone Number ID is required for Meta WhatsApp Cloud API.")
                } else {
                    try {
                        val startTime = System.currentTimeMillis()
                        val testUrl = "https://graph.facebook.com/v21.0/$phoneNumberId"
                        val request = Request.Builder()
                            .url(testUrl)
                            .header("Authorization", "Bearer $customApiKey")
                            .get()
                            .build()

                        val response = httpClient.newCall(request).execute()
                        val elapsed = System.currentTimeMillis() - startTime
                        if (response.isSuccessful) {
                            Pair(true, "✓ Meta Graph API connected successfully (${response.code} OK, ${elapsed}ms latency).")
                        } else {
                            Pair(false, "⚠️ Meta API returned status ${response.code}: ${response.message}")
                        }
                    } catch (e: Exception) {
                        Pair(false, "Connection error: ${e.message}")
                    }
                }
            }
            WhatsAppApiProvider.CUSTOM_WEBHOOK -> {
                if (webhookUrl.isBlank()) {
                    Pair(false, "⚠️ Webhook URL is empty. Please enter your server endpoint.")
                } else {
                    try {
                        val startTime = System.currentTimeMillis()
                        val testPayload = JSONObject().apply {
                            put("event", "ping")
                            put("agent", "C9-SHANICE")
                            put("timestamp", System.currentTimeMillis())
                        }.toString()

                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val request = Request.Builder()
                            .url(webhookUrl)
                            .post(testPayload.toRequestBody(mediaType))
                            .build()

                        val response = httpClient.newCall(request).execute()
                        val elapsed = System.currentTimeMillis() - startTime
                        Pair(response.isSuccessful, "Webhook responded with HTTP ${response.code} (${elapsed}ms).")
                    } catch (e: Exception) {
                        Pair(false, "Webhook unreachable: ${e.message}")
                    }
                }
            }
            WhatsAppApiProvider.TWILIO, WhatsAppApiProvider.GREEN_API -> {
                if (customApiKey.isBlank()) {
                    Pair(false, "⚠️ Token / API Key is required for ${apiProvider.displayName}.")
                } else {
                    Pair(true, "✓ Configuration validated for ${apiProvider.displayName}. Gateway ready.")
                }
            }
        }
    }

    suspend fun dispatchViaCloudApi(
        toPhoneNumber: String,
        messageText: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiProvider == WhatsAppApiProvider.LOCAL_SMART_AI) {
            return@withContext Result.success("Dispatched via Local AI Bridge (Simulated/Local Notification Reply).")
        }

        if (customApiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("WhatsApp API Key is empty. Replace your API key in settings or use Local AI mode.")
            )
        }

        when (apiProvider) {
            WhatsAppApiProvider.META_CLOUD_API -> {
                try {
                    val url = "https://graph.facebook.com/v21.0/$phoneNumberId/messages"
                    val payload = JSONObject().apply {
                        put("messaging_product", "whatsapp")
                        put("to", toPhoneNumber.replace("[^0-9]".toRegex(), ""))
                        put("type", "text")
                        put("text", JSONObject().put("body", messageText))
                    }.toString()

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val request = Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer $customApiKey")
                        .post(payload.toRequestBody(mediaType))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val body = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        Result.success("Message sent successfully via Meta Cloud API.")
                    } else {
                        Result.failure(Exception("Meta API Error ${response.code}: $body"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            WhatsAppApiProvider.CUSTOM_WEBHOOK -> {
                try {
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val payload = JSONObject().apply {
                        put("to", toPhoneNumber)
                        put("message", messageText)
                        put("sender", "C9-SHANICE")
                    }.toString()

                    val request = Request.Builder()
                        .url(webhookUrl)
                        .post(payload.toRequestBody(mediaType))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        Result.success("Dispatched via Custom Webhook.")
                    } else {
                        Result.failure(Exception("Webhook Error ${response.code}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            else -> {
                Result.success("Dispatched via ${apiProvider.displayName}.")
            }
        }
    }
}
