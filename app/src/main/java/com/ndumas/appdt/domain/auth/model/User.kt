package com.ndumas.appdt.domain.auth.model

data class User(
    val userId: Int,
    val email: String,
    val username: String?,
)
