package com.example.dosely.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dosely.data.MedicationDao
import com.example.dosely.data.MedicationEntity
import com.example.dosely.data.DoseStatusEntity
import com.example.dosely.data.DoseStatusDao
import com.example.dosely.data.ReminderEntity
import com.example.dosely.data.ReminderDao

import android.content.Context
import androidx.room.Room

@Database(
    entities = [MedicationEntity::class, DoseStatusEntity::class, ReminderEntity::class],
    version = 1
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
