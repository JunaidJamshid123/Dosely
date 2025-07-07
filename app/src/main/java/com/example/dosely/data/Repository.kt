package com.example.dosely.data

import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.LiveData

// Room MedicationEntity and DAO will be defined separately

interface MedicationRepository {
    fun getAllMedications(): Flow<List<MedicationEntity>>
    suspend fun insertMedication(medication: MedicationEntity)
    suspend fun updateMedication(medication: MedicationEntity)
    suspend fun deleteMedication(medication: MedicationEntity)
}

class MedicationRepositoryImpl(private val dao: MedicationDao) : MedicationRepository {
    override fun getAllMedications(): Flow<List<MedicationEntity>> = dao.getAllMedications()
    override suspend fun insertMedication(medication: MedicationEntity) = dao.insertMedication(medication)
    override suspend fun updateMedication(medication: MedicationEntity) = dao.updateMedication(medication)
    override suspend fun deleteMedication(medication: MedicationEntity) = dao.deleteMedication(medication)
}

class Repository(
    private val apiService: ApiService,
    private val exampleDao: ExampleDao
) {
    // Remote data
    suspend fun fetchRemoteData(): List<String> = apiService.getExampleData()

    // Local data
    suspend fun getLocalData(): List<ExampleEntity> = exampleDao.getAll()
    suspend fun insertLocalData(entity: ExampleEntity) = exampleDao.insert(entity)
} 