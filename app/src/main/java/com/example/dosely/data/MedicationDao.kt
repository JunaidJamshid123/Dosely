package com.example.dosely.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity(tableName = "dose_status")
data class DoseStatusEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val time: String, // e.g., "08:00 AM"
    val date: String, // ISO_LOCAL_DATE (yyyy-MM-dd)
    val isTaken: Boolean = false
)

@Dao
interface DoseStatusDao {
    @Query("SELECT * FROM dose_status WHERE medicationId = :medicationId AND time = :time AND date = :date LIMIT 1")
    suspend fun getDoseStatus(medicationId: Int, time: String, date: String): DoseStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseStatus(status: DoseStatusEntity)

    @Query("SELECT * FROM dose_status WHERE date = :date")
    fun getAllDoseStatusForDate(date: String): Flow<List<DoseStatusEntity>>
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY id DESC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)
} 