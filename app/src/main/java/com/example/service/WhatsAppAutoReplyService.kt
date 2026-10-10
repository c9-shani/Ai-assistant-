package com.example.service

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.agent.WhatsAppBotManager
import com.example.data.api.GeminiAgentApi
import com.example.data.database.AgentDatabase
import com.example.data.database.WhatsAppMessageLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WhatsAppAutoReplyService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AgentDatabase
    private lateinit var botManager: WhatsAppBotManager

    override fun onCreate() {
        super.onCreate()
        database = AgentDatabase.getDatabase(this)
        botManager = WhatsAppBotManager(this, GeminiAgentApi())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: ""
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") {
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val sender = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Ignore summary/group messages with empty content
        if (sender.isBlank() || message.isBlank() || sender.contains("WhatsApp") || sender.contains("messages")) {
            return
        }

        serviceScope.launch {
            try {
                val rules = emptyList<com.example.data.database.WhatsAppRuleEntity>()
                val reply = botManager.generateAiReply(message, sender, rules)

                // Log the interaction
                database.whatsAppDao().insertLog(
                    WhatsAppMessageLog(
                        sender = sender,
                        incomingText = message,
                        replyText = reply,
                        status = "AUTO-REPLIED"
                    )
                )

                // Attempt to send inline reply through RemoteInput if available in notification actions
                notification.actions?.forEach { action ->
                    action.remoteInputs?.forEach { remoteInput ->
                        val intent = Intent()
                        val bundle = Bundle()
                        bundle.putCharSequence(remoteInput.resultKey, reply)
                        RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, bundle)
                        try {
                            action.actionIntent.send(this@WhatsAppAutoReplyService, 0, intent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            } catch (e: Exception) {
                // error handling
            }
        }
    }
}
