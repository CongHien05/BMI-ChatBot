package com.hienpc.bmiapp.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.data.repository.ChatRepository
import com.hienpc.bmiapp.databinding.FragmentChatHistoryBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatHistoryFragment : Fragment() {

    private var _binding: FragmentChatHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ChatRepository
    private lateinit var adapter: ChatConversationAdapter

    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = ChatRepository.withLocal(requireContext())

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = ChatConversationAdapter { conversationId ->
            // Khi click vào một conversation, mở ChatFragment với conversationId đó
            val chatFragment = ChatFragment().apply {
                arguments = Bundle().apply {
                    putLong("conversation_id", conversationId)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, chatFragment)
                .addToBackStack("chat_conversation")
                .commit()
        }

        binding.recyclerConversations.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerConversations.adapter = adapter

        loadConversations()
    }

    private fun loadConversations() {
        binding.progressBar.visibility = View.VISIBLE
        binding.textEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = repository.getConversations()
                if (response.isSuccessful) {
                    val body = response.body().orEmpty()
                    if (body.isEmpty()) {
                        binding.textEmpty.visibility = View.VISIBLE
                        adapter.submitList(emptyList())
                    } else {
                        val uiList = body.map { dto ->
                            ChatConversationUiModel(
                                conversationId = dto.conversationId,
                                lastMessagePreview = dto.lastMessagePreview,
                                lastTime = formatTime(dto.lastTime),
                                totalMessages = dto.totalMessages
                            )
                        }
                        adapter.submitList(uiList)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Lỗi tải lịch sử: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.textEmpty.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Lỗi kết nối: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.textEmpty.visibility = View.VISIBLE
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun formatTime(iso: String): String {
        return try {
            // Backend trả ISO datetime, parse và format lại
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(iso)
            if (date != null) {
                val now = Date()
                val diff = now.time - date.time
                val daysDiff = diff / (1000 * 60 * 60 * 24)

                when {
                    daysDiff == 0L -> timeFormatter.format(date) // Hôm nay: chỉ hiển thị giờ
                    daysDiff == 1L -> "Hôm qua"
                    daysDiff < 7L -> dateFormatter.format(date) // Trong tuần: hiển thị ngày
                    else -> dateFormatter.format(date) // Cũ hơn: hiển thị ngày
                }
            } else {
                iso.takeLast(5) // Fallback: lấy 5 ký tự cuối (HH:mm)
            }
        } catch (e: Exception) {
            iso.takeLast(5) // Fallback
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

