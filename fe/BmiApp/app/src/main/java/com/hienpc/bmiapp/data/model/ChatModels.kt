package com.hienpc.bmiapp.data.model

data class ChatRequestDto(
    val message: String,
    val conversationId: Long? = null
)

data class ChatResponseDto(
    val reply: String,
    val source: String?,
    val traceId: String?,
    val durationMs: Long?,
    val conversationId: Long?
)

data class ChatHistoryItemDto(
    val sender: String,
    val content: String,
    val createdAt: String
)

data class ChatConversationSummaryDto(
    val conversationId: Long,
    val lastMessagePreview: String,
    val lastSender: String,
    val lastTime: String,
    val totalMessages: Long
)
