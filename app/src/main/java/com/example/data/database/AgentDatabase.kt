package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MissionEntity::class,
        MemoryEntity::class,
        WhatsAppRuleEntity::class,
        WhatsAppMessageLog::class,
        WhatsAppConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun missionDao(): MissionDao
    abstract fun memoryDao(): MemoryDao
    abstract fun whatsAppDao(): WhatsAppDao

    companion object {
        @Volatile
        private var INSTANCE: AgentDatabase? = null

        fun getDatabase(context: Context): AgentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgentDatabase::class.java,
                    "c9_shanice_agent.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
