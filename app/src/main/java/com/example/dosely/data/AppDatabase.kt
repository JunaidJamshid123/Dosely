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

@Database(entities = [MedicationEntity::class, DoseStatusEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseStatusDao(): DoseStatusDao
} 