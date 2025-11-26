package com.hienpc.bmiapp.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hienpc.bmiapp.data.model.ChatMessage
import com.hienpc.bmiapp.databinding.FragmentChatBinding

/**
 * ChatFragment - UI chat với RecyclerView và chat bubbles
 */
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private val chatAdapter = ChatAdapter()
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true // Scroll từ dưới lên
        }
        binding.recyclerViewChat.adapter = chatAdapter

        // Thêm welcome message
        if (messages.isEmpty()) {
            addBotMessage("Xin chào! Tôi là chatbot hỗ trợ sức khỏe. Bạn cần tôi giúp gì?")
        }

        // Send button click
        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                addUserMessage(text)
                binding.editTextMessage.text?.clear()
                
                // Mock: Bot response sau 500ms
                binding.root.postDelayed({
                    addBotMessage("Cảm ơn bạn đã gửi tin nhắn. Tính năng này đang được phát triển!")
                }, 500)
            }
        }
    }

    private fun addUserMessage(text: String) {
        val message = ChatMessage(text = text, isUser = true)
        messages.add(message)
        chatAdapter.submitList(messages.toList())
        scrollToBottom()
    }

    private fun addBotMessage(text: String) {
        val message = ChatMessage(text = text, isUser = false)
        messages.add(message)
        chatAdapter.submitList(messages.toList())
        scrollToBottom()
    }

    private fun scrollToBottom() {
        binding.recyclerViewChat.post {
            val itemCount = chatAdapter.itemCount
            if (itemCount > 0) {
                binding.recyclerViewChat.smoothScrollToPosition(itemCount - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
