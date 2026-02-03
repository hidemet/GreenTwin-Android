package com.ndumas.appdt.domain.service.model

data class ServiceRequest(
    val entityId: String,
    val service: String, // Es: "turn_on" o "light.turn_on"
    val data: Map<String, Any>? = null,
    val userId: String? = null,
)
