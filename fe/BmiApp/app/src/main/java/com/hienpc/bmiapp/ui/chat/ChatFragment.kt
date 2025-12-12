package com.hienpc.bmiapp.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.databinding.FragmentChatBinding
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.viewmodel.ChatViewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                    return ChatViewModel(
                        com.hienpc.bmiapp.data.repository.ChatRepository.withLocal(requireContext())
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

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

        val adapter = ChatMessageAdapter()
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.recyclerMessages.adapter = adapter

        // Quan sát messages từ ViewModel
        viewModel.messages.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list) {
                // Smooth scroll to bottom after list updated
                if (list.isNotEmpty()) {
                    binding.recyclerMessages.smoothScrollToPosition(list.size - 1)
                }
            }
        }

        // Quan sát trạng thái gửi
        viewModel.sendState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.buttonSend.isEnabled = false
                    binding.textTypingIndicator.visibility = View.VISIBLE
                    
                    // Apply pulsing animation to typing indicator
                    val animation = android.view.animation.AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.typing_indicator
                    )
                    binding.textTypingIndicator.startAnimation(animation)
                }
                is UiState.Success -> {
                    binding.buttonSend.isEnabled = true
                    binding.editMessage.text = null
                    binding.textTypingIndicator.clearAnimation()
                    binding.textTypingIndicator.visibility = View.GONE
                    viewModel.resetSendState()
                }
                is UiState.Error -> {
                    binding.buttonSend.isEnabled = true
                    binding.textTypingIndicator.clearAnimation()
                    binding.textTypingIndicator.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetSendState()
                }
                else -> {
                    binding.buttonSend.isEnabled = true
                }
            }
        }

        binding.textNewConversation.setOnClickListener {
            viewModel.startNewConversation()
        }

        binding.buttonSend.setOnClickListener {
            val text = binding.editMessage.text?.toString().orEmpty()
            viewModel.sendMessage(text)
            hideKeyboard()
        }

        binding.editMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val text = binding.editMessage.text?.toString().orEmpty()
                viewModel.sendMessage(text)
                hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.buttonHistory.setOnClickListener {
            // Mở màn lịch sử chat
            val historyFragment = ChatHistoryFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, historyFragment)
                .addToBackStack("chat_history")
                .commit()
        }

        // Kiểm tra xem có conversationId từ arguments không (khi mở từ lịch sử)
        val conversationId = arguments?.getLong("conversation_id", -1L)
        if (conversationId != null && conversationId > 0) {
            // Load conversation cụ thể
            viewModel.observeLocalHistory()
            viewModel.loadConversation(conversationId)
        } else {
            // Load recent history như bình thường
            viewModel.observeLocalHistory()
            viewModel.loadRemoteHistory()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editMessage.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


