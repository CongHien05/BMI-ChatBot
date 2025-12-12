package com.hienpc.bmiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hienpc.bmiapp.data.local.entity.ChatMessageEntity
import com.hienpc.bmiapp.data.repository.ChatRepository
import com.hienpc.bmiapp.ui.chat.ChatMessageUiModel
import com.hienpc.bmiapp.utils.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessageUiModel>>(emptyList())
    val messages: LiveData<List<ChatMessageUiModel>> = _messages

    private val _sendState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val sendState: LiveData<UiState<Unit>> = _sendState

    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var currentConversationId: Long? = null

    fun observeLocalHistory() {
        viewModelScope.launch {
            repository.observeLocalMessages()?.collectLatest { entities ->
                _messages.value = entities.map { it.toUiModel() }
            }
        }
    }

    fun loadRemoteHistory() {
        viewModelScope.launch {
            try {
                val response = repository.getHistory()
                if (response.isSuccessful) {
                    val body = response.body().orEmpty()
                    repository.saveLocalMessages(body)
                }
            } catch (_: Exception) {
                // bỏ qua, có thể hiển thị toast ở Fragment nếu cần
            }
        }
    }

    /**
     * Load messages của một conversation cụ thể từ backend.
     * Dùng khi user click vào một conversation trong lịch sử.
     */
    fun loadConversation(conversationId: Long) {
        currentConversationId = conversationId
        viewModelScope.launch {
            try {
                val response = repository.getConversationMessages(conversationId)
                if (response.isSuccessful) {
                    val body = response.body().orEmpty()
                    // Clear local trước khi load conversation mới
                    repository.clearLocalMessages()
                    // Lưu messages vào local
                    repository.saveLocalMessages(body)
                    // Convert sang UI model
                    val uiList = body.mapIndexed { index, item ->
                        ChatMessageUiModel(
                            id = index.toLong(),
                            sender = if (item.sender.uppercase() == "USER") {
                                ChatMessageUiModel.Sender.USER
                            } else {
                                ChatMessageUiModel.Sender.BOT
                            },
                            content = item.content,
                            time = formatTime(item.createdAt)
                        )
                    }
                    _messages.value = uiList
                }
            } catch (_: Exception) {
                // bỏ qua, có thể hiển thị toast ở Fragment nếu cần
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _sendState.value = UiState.Loading

            // Thêm message của user vào UI ngay (optimistic)
            val currentList = _messages.value.orEmpty().toMutableList()
            val userMessageId = (currentList.lastOrNull()?.id ?: 0L) + 1L
            val now = timeFormatter.format(Date())

            val userMessage = ChatMessageUiModel(
                id = userMessageId,
                sender = ChatMessageUiModel.Sender.USER,
                content = text,
                time = now
            )
            currentList.add(userMessage)
            _messages.value = currentList
            repository.saveLocalMessage("USER", text, now)

            try {
                val response = repository.sendMessage(text, currentConversationId)
                if (response.isSuccessful) {
                    val body = response.body()
                    // Cập nhật conversationId từ backend nếu có
                    if (body?.conversationId != null) {
                        currentConversationId = body.conversationId
                    }
                    val botMessageId = userMessageId + 1
                    val botMessage = ChatMessageUiModel(
                        id = botMessageId,
                        sender = ChatMessageUiModel.Sender.BOT,
                        content = body?.reply ?: "Xin lỗi, mình chưa nhận được câu trả lời.",
                        time = now
                    )
                    currentList.add(botMessage)
                    _messages.value = currentList
                    repository.saveLocalMessage("BOT", botMessage.content, now)

                    _sendState.value = UiState.Success(Unit)
                } else {
                    _sendState.value = UiState.Error(
                        response.message().ifBlank { "Gửi tin nhắn thất bại" }
                    )
                }
            } catch (e: Exception) {
                _sendState.value = UiState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun resetSendState() {
        _sendState.value = UiState.Idle
    }

    private fun formatTime(iso: String): String {
        // Backend trả ISO datetime, ở đây chỉ cần hh:mm nên đơn giản lấy phần cuối nếu đủ dài
        return if (iso.length >= 5) iso.takeLast(5) else ""
    }

    private fun ChatMessageEntity.toUiModel(): ChatMessageUiModel {
        val senderEnum = if (sender.uppercase() == "USER") {
            ChatMessageUiModel.Sender.USER
        } else {
            ChatMessageUiModel.Sender.BOT
        }
        return ChatMessageUiModel(
            id = id,
            sender = senderEnum,
            content = content,
            time = createdAt
        )
    }

    /**
     * Bắt đầu một cuộc trò chuyện mới trên UI.
     * Xóa lịch sử local để tránh load trộn tin cũ + mới trên thiết bị.
     * Lịch sử đầy đủ vẫn nằm trên backend nếu cần xem lại.
     */
    fun startNewConversation() {
        currentConversationId = null
        _messages.value = emptyList()
        viewModelScope.launch {
            try {
                repository.clearLocalMessages()
            } catch (_: Exception) {
                // ignore local clear error
            }
        }
    }
}


