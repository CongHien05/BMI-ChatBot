package com.hienpc.bmiapp.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hienpc.bmiapp.databinding.ItemChatConversationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatConversationUiModel(
    val conversationId: Long,
    val lastMessagePreview: String,
    val lastTime: String,
    val totalMessages: Long
)

class ChatConversationAdapter(
    private val onItemClick: (Long) -> Unit
) : ListAdapter<ChatConversationUiModel, ChatConversationAdapter.ViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ChatConversationUiModel>() {
            override fun areItemsTheSame(
                oldItem: ChatConversationUiModel,
                newItem: ChatConversationUiModel
            ): Boolean = oldItem.conversationId == newItem.conversationId

            override fun areContentsTheSame(
                oldItem: ChatConversationUiModel,
                newItem: ChatConversationUiModel
            ): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemChatConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatConversationUiModel) {
            binding.textLastMessage.text = item.lastMessagePreview.ifEmpty { "Không có tin nhắn" }
            binding.textTime.text = item.lastTime
            binding.textTotalMessages.text = "${item.totalMessages} tin nhắn"

            binding.root.setOnClickListener {
                onItemClick(item.conversationId)
            }
        }
    }
}

