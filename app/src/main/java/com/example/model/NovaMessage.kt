package com.example.model

enum class MessageRole {
    USER,
    ASSISTANT
}

data class NovaMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionUrl: String? = null,
    val actionLabel: String? = null,
    val intent: String? = null
)
