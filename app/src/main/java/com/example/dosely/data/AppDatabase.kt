package com.example.dosely.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MedicationEntity::class, DoseStatusEntity::class, ReminderEntity::class],
    version = 2, // ✅ Increase version to fix schema mismatch crash
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseStatusDao(): DoseStatusDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dosely_database"
                )
                    .fallbackToDestructiveMigration() // ✅ Prevent crashing due to migration issues
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
