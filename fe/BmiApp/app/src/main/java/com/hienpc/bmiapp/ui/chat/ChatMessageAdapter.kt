package com.hienpc.bmiapp.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.databinding.ItemChatMessageBotBinding
import com.hienpc.bmiapp.databinding.ItemChatMessageUserBinding

class ChatMessageAdapter :
    ListAdapter<ChatMessageUiModel, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2

        private val DiffCallback = object : DiffUtil.ItemCallback<ChatMessageUiModel>() {
            override fun areItemsTheSame(
                oldItem: ChatMessageUiModel,
                newItem: ChatMessageUiModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: ChatMessageUiModel,
                newItem: ChatMessageUiModel
            ): Boolean = oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).sender) {
            ChatMessageUiModel.Sender.USER -> VIEW_TYPE_USER
            ChatMessageUiModel.Sender.BOT -> VIEW_TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val binding = ItemChatMessageUserBinding.inflate(inflater, parent, false)
                UserMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemChatMessageBotBinding.inflate(inflater, parent, false)
                BotMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is UserMessageViewHolder -> holder.bind(item, position)
            is BotMessageViewHolder -> holder.bind(item, position)
        }
    }
    
    // Track last animated position để tránh animate lại khi scroll
    private var lastAnimatedPosition = -1
    
    fun resetAnimation() {
        lastAnimatedPosition = -1
    }

    inner class UserMessageViewHolder(
        private val binding: ItemChatMessageUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessageUiModel, position: Int) {
            binding.textMessage.text = item.content
            binding.textTime.text = item.time
            
            // Apply animation cho new messages
            if (position > lastAnimatedPosition) {
                val animation = AnimationUtils.loadAnimation(
                    binding.root.context,
                    R.anim.message_send
                )
                binding.root.startAnimation(animation)
                lastAnimatedPosition = position
            }
        }
    }

    inner class BotMessageViewHolder(
        private val binding: ItemChatMessageBotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessageUiModel, position: Int) {
            binding.textMessage.text = item.content
            binding.textTime.text = item.time
            
            // Apply animation cho new messages
            if (position > lastAnimatedPosition) {
                val animation = AnimationUtils.loadAnimation(
                    binding.root.context,
                    R.anim.message_receive
                )
                binding.root.startAnimation(animation)
                lastAnimatedPosition = position
            }
        }
    }
}


