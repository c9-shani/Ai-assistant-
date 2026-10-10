package com.example.data.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "whatsapp_rules")
data class WhatsAppRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyword: String,
    val response: String,
    val isAiPowered: Boolean = true,
    val isEnabled: Boolean = true
)

@Entity(tableName = "whatsapp_logs")
data class WhatsAppMessageLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val incomingText: String,
    val replyText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "REPLIED"
)

@Entity(tableName = "whatsapp_config")
data class WhatsAppConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val apiKey: String = "",
    val phoneNumberId: String = "",
    val webhookUrl: String = "",
    val provider: String = "LOCAL_SMART_AI",
    val persona: String = "CUSTOMER_SUPPORT",
    val customBusinessPrompt: String = "",
    val isAutoReplyEnabled: Boolean = true,
    val replyDelaySeconds: Int = 1
)

@Dao
interface WhatsAppDao {
    @Query("SELECT * FROM whatsapp_rules ORDER BY id DESC")
    fun getAllRules(): Flow<List<WhatsAppRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: WhatsAppRuleEntity): Long

    @Query("DELETE FROM whatsapp_rules WHERE id = :id")
    suspend fun deleteRule(id: Long)

    @Query("UPDATE whatsapp_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun toggleRule(id: Long, enabled: Boolean)

    @Query("SELECT * FROM whatsapp_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllLogs(): Flow<List<WhatsAppMessageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WhatsAppMessageLog): Long

    @Query("DELETE FROM whatsapp_logs")
    suspend fun clearLogs()

    @Query("SELECT * FROM whatsapp_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<WhatsAppConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: WhatsAppConfigEntity)
}
