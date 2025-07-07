package com.example.dosely.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val description: String = "",
    val frequency: Int,
    val times: String, // Store as comma-separated string for simplicity
    val duration: Int,
    val notes: String = "",
    val medicationType: String = "Tablet",
    val startDate: String = "",
    val endDate: String = ""
)

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

interface ApiService {
    @GET("example-endpoint")
    suspend fun getExampleData(): List<String>
} 