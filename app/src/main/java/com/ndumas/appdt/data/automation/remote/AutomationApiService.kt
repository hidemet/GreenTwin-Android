package com.ndumas.appdt.data.automation.remote

import com.ndumas.appdt.data.automation.remote.dto.AutomationDto
import com.ndumas.appdt.data.automation.remote.dto.AutomationRequestDto
import com.ndumas.appdt.data.automation.remote.dto.SimulationResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AutomationApiService {
    @POST("automation")
    suspend fun createAutomation(
        @Body request: AutomationRequestDto,
    ): Any

    @GET("automation")
    suspend fun getAutomations(): List<AutomationDto>

    @POST("automation/simulate")
    suspend fun simulateAutomation(
        @Body request: AutomationRequestDto,
        @Query("return_state_matrix") returnStateMatrix: Boolean = false,
    ): SimulationResponseDto
}
