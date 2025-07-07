package com.example.dosely.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val description: String = "",
    val frequency: Int,
    val duration: Int,
    val notes: String = "",
    val medicationType: String = "Tablet",
    val times: String = ""
) 