package com.example.dosely.data

import retrofit2.http.GET

interface ApiService {
    @GET("example-endpoint")
    suspend fun getExampleData(): List<String>
} 