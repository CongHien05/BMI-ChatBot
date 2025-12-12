package com.hienpc.bmiapp.ui.chat

data class ChatMessageUiModel(
    val id: Long,
    val sender: Sender,
    val content: String,
    val time: String
) {
    enum class Sender {
        USER, BOT
    }
}


