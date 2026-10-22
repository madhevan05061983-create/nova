package com.example.model

data class UserSession(
    val name: String = "Operator",
    val email: String = "madhevan05061983@gmail.com",
    val avatarUrl: String? = null,
    val isSignedIn: Boolean = false
)
