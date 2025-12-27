package com.hienpc.bmiapp.ui.main.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse
import com.hienpc.bmiapp.databinding.ItemExerciseHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying exercise log history items
 */
class ExerciseHistoryAdapter(
    private val onEditClick: (ExerciseLogHistoryResponse) -> Unit,
    private val onDeleteClick: (ExerciseLogHistoryResponse) -> Unit
) : ListAdapter<ExerciseLogHistoryResponse, ExerciseHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExerciseHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateItems(newItems: List<ExerciseLogHistoryResponse>) {
        submitList(newItems)
    }

    class ViewHolder(
        private val binding: ItemExerciseHistoryBinding,
        private val onEditClick: (ExerciseLogHistoryResponse) -> Unit,
        private val onDeleteClick: (ExerciseLogHistoryResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExerciseLogHistoryResponse) {
            binding.textExerciseName.text = item.exerciseName
            binding.textDuration.text = "${item.duration.toInt()} phút"
            binding.textCalories.text = "${item.totalCaloriesBurned} kcal"

            // Format date (API 24+ compatible)
            try {
                // Parse ISO datetime format: "2025-12-14T12:30:00"
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(item.dateExercised)
                if (date != null) {
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    binding.textDate.text = outputFormat.format(date)
                } else {
                    binding.textDate.text = item.dateExercised
                }
            } catch (e: Exception) {
                binding.textDate.text = item.dateExercised
            }

            // Check if log is from today (can edit/delete)
            val isToday = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(item.dateExercised)
                if (date != null) {
                    val today = Calendar.getInstance()
                    val logDate = Calendar.getInstance()
                    logDate.time = date
                    today.get(Calendar.YEAR) == logDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == logDate.get(Calendar.DAY_OF_YEAR)
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }

            // Show/hide edit/delete buttons based on date
            if (isToday) {
                binding.buttonEdit.visibility = ViewGroup.VISIBLE
                binding.buttonDelete.visibility = ViewGroup.VISIBLE
            } else {
                binding.buttonEdit.visibility = ViewGroup.GONE
                binding.buttonDelete.visibility = ViewGroup.GONE
            }

            binding.buttonEdit.setOnClickListener { onEditClick(item) }
            binding.buttonDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ExerciseLogHistoryResponse>() {
        override fun areItemsTheSame(
            oldItem: ExerciseLogHistoryResponse,
            newItem: ExerciseLogHistoryResponse
        ): Boolean = oldItem.logId == newItem.logId

        override fun areContentsTheSame(
            oldItem: ExerciseLogHistoryResponse,
            newItem: ExerciseLogHistoryResponse
        ): Boolean = oldItem == newItem
    }
}

