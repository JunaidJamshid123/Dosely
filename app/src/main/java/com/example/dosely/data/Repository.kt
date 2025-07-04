package com.example.dosely.data

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