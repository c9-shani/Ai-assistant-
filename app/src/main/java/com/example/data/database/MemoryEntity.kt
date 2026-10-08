package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String, // SYSTEM, USER, TOOL, BENCHMARK
    val timestamp: Long = System.currentTimeMillis()
)
