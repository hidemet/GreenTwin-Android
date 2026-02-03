package com.ndumas.appdt.data.auth.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "user_id") val userId: Int,
    val email: String,
    val username: String? = null,
)
