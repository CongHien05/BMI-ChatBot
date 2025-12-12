package com.hienpc.bmiapp.data.repository

import com.hienpc.bmiapp.data.local.dao.ChatMessageDao
import com.hienpc.bmiapp.data.local.database.AppDatabase
import com.hienpc.bmiapp.data.local.entity.ChatMessageEntity
import com.hienpc.bmiapp.data.model.ChatConversationSummaryDto
import com.hienpc.bmiapp.data.model.ChatHistoryItemDto
import com.hienpc.bmiapp.data.model.ChatRequestDto
import com.hienpc.bmiapp.data.model.ChatResponseDto
import com.hienpc.bmiapp.data.network.ApiClient
import com.hienpc.bmiapp.data.network.ChatApiService
import retrofit2.Response

class ChatRepository(
    private val api: ChatApiService = ApiClient.createService(ChatApiService::class.java),
    private val chatMessageDao: ChatMessageDao? = null
) {

    suspend fun sendMessage(message: String, conversationId: Long?): Response<ChatResponseDto> {
        return api.chat(ChatRequestDto(message = message, conversationId = conversationId))
    }

    suspend fun getHistory(): Response<List<ChatHistoryItemDto>> {
        return api.getHistory()
    }

    suspend fun getConversations(): Response<List<ChatConversationSummaryDto>> {
        return api.getConversations()
    }

    suspend fun getConversationMessages(conversationId: Long): Response<List<ChatHistoryItemDto>> {
        return api.getConversationMessages(conversationId)
    }

    // LOCAL (Room)
    fun observeLocalMessages() = chatMessageDao?.getAllMessagesFlow()

    suspend fun saveLocalMessage(sender: String, content: String, createdAt: String) {
        chatMessageDao?.insert(
            ChatMessageEntity(
                sender = sender,
                content = content,
                createdAt = createdAt
            )
        )
    }

    suspend fun saveLocalMessages(items: List<ChatHistoryItemDto>) {
        if (chatMessageDao == null) return
        val entities = items.map {
            ChatMessageEntity(
                sender = it.sender,
                content = it.content,
                createdAt = it.createdAt
            )
        }
        chatMessageDao.insertAll(entities)
    }

    suspend fun clearLocalMessages() {
        chatMessageDao?.clearAll()
    }

    companion object {
        fun withLocal(context: android.content.Context): ChatRepository {
            val db = AppDatabase.getInstance(context)
            return ChatRepository(
                api = ApiClient.createService(ChatApiService::class.java),
                chatMessageDao = db.chatMessageDao()
            )
        }
    }
}


