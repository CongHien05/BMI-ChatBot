package com.hienpc.bmiapp.data.network

import com.hienpc.bmiapp.data.model.ChatConversationSummaryDto
import com.hienpc.bmiapp.data.model.ChatHistoryItemDto
import com.hienpc.bmiapp.data.model.ChatRequestDto
import com.hienpc.bmiapp.data.model.ChatResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApiService {

    @POST("api/chatbot")
    suspend fun chat(@Body request: ChatRequestDto): Response<ChatResponseDto>

    @GET("api/chatbot/history")
    suspend fun getHistory(): Response<List<ChatHistoryItemDto>>

    @GET("api/chatbot/conversations")
    suspend fun getConversations(): Response<List<ChatConversationSummaryDto>>

    @GET("api/chatbot/conversations/{conversationId}")
    suspend fun getConversationMessages(@Path("conversationId") conversationId: Long): Response<List<ChatHistoryItemDto>>
}


