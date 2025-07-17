package com.example.dosely.data

import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.LiveData
import java.time.LocalDate
import com.example.dosely.data.DoseStatusEntity

// Room MedicationEntity and DAO will be defined separately

interface MedicationRepository {
    fun getAllMedications(): Flow<List<MedicationEntity>>
    suspend fun insertMedication(medication: MedicationEntity)
    suspend fun updateMedication(medication: MedicationEntity)
    suspend fun deleteMedication(medication: MedicationEntity)
    // Dose status
    suspend fun getDoseStatus(medicationId: Int, time: String, date: String): DoseStatusEntity?
    suspend fun setDoseStatus(status: DoseStatusEntity)
    fun getAllDoseStatusForDate(date: String): Flow<List<DoseStatusEntity>>
}

class MedicationRepositoryImpl(private val dao: MedicationDao, private val doseStatusDao: DoseStatusDao) : MedicationRepository {
    override fun getAllMedications(): Flow<List<MedicationEntity>> = dao.getAllMedications()
    override suspend fun insertMedication(medication: MedicationEntity) = dao.insertMedication(medication)
    override suspend fun updateMedication(medication: MedicationEntity) = dao.updateMedication(medication)
    override suspend fun deleteMedication(medication: MedicationEntity) = dao.deleteMedication(medication)
    // Dose status
    override suspend fun getDoseStatus(medicationId: Int, time: String, date: String): DoseStatusEntity? =
        doseStatusDao.getDoseStatus(medicationId, time, date)
    override suspend fun setDoseStatus(status: DoseStatusEntity) =
        doseStatusDao.insertDoseStatus(status)
    override fun getAllDoseStatusForDate(date: String): Flow<List<DoseStatusEntity>> =
        doseStatusDao.getAllDoseStatusForDate(date)
}