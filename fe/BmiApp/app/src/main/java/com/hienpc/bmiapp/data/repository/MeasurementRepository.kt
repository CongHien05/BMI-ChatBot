package com.hienpc.bmiapp.data.repository

import com.hienpc.bmiapp.data.model.MeasurementRequest
import com.hienpc.bmiapp.data.network.ApiClient
import com.hienpc.bmiapp.data.network.ApiService

class MeasurementRepository(
    private val apiService: ApiService = ApiClient.createService(ApiService::class.java)
) {
    suspend fun addMeasurement(request: MeasurementRequest) = apiService.addMeasurement(request)

    suspend fun getLatestMeasurement() = apiService.getLatestMeasurement()
}

