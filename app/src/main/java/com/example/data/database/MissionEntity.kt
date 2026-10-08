package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val goalPrompt: String,
    val status: String, // COMPLETED, FAILED, RUNNING
    val totalSteps: Int,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis()
)
