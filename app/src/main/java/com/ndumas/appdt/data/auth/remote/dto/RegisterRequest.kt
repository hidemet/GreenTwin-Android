package com.ndumas.appdt.data.auth.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @Json(name = "use_sqlite") val useSqlite: Boolean = true,
)
