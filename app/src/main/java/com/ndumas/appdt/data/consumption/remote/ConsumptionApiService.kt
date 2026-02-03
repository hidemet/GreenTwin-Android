package com.ndumas.appdt.data.consumption.remote

import com.ndumas.appdt.data.consumption.remote.dto.ConsumptionDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ConsumptionApiService {
    @GET("consumption/total")
    suspend fun getTotalConsumption(
        @Query("start_timestamp") startDate: String, // YYYY-MM-DD
        @Query("end_timestamp") endDate: String,
        @Query("group") group: String = "hourly", // "hourly", "daily", "monthly"
    ): List<ConsumptionDto>

    @GET("consumption/entity")
    suspend fun getEntitiesConsumption(
        @Query("entities") entityIds: String,
        @Query("start_timestamp") startDate: String,
        @Query("end_timestamp") endDate: String,
        @Query("group") group: String = "entity",
    ): List<ConsumptionDto>
}
