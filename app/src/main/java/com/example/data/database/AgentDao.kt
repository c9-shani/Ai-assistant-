package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions ORDER BY timestamp DESC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Query("DELETE FROM missions WHERE id = :id")
    suspend fun deleteMission(id: Long)

    @Query("DELETE FROM missions")
    suspend fun clearAllMissions()
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM agent_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Query("DELETE FROM agent_memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM agent_memories")
    suspend fun clearAllMemories()
}
