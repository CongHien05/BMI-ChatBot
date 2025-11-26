package com.hienpc.bmiapp.data.model

/**
 * ChatMessage - Data class cho tin nhắn trong chat
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean, // true = user message, false = bot message
    val timestamp: Long = System.currentTimeMillis()
)

