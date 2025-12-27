package com.hienpc.bmiapp.ui.main.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hienpc.bmiapp.databinding.ItemExerciseHistoryBinding
import com.hienpc.bmiapp.databinding.ItemFoodHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying mixed food and exercise logs in a single list
 */
class DailyLogsAdapter(
    private val onFoodEditClick: (com.hienpc.bmiapp.data.model.FoodLogHistoryResponse) -> Unit,
    private val onFoodDeleteClick: (com.hienpc.bmiapp.data.model.FoodLogHistoryResponse) -> Unit,
    private val onExerciseEditClick: (com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse) -> Unit,
    private val onExerciseDeleteClick: (com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse) -> Unit
) : ListAdapter<LogItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_FOOD = 0
        private const val VIEW_TYPE_EXERCISE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is LogItem.FoodLog -> VIEW_TYPE_FOOD
            is LogItem.ExerciseLog -> VIEW_TYPE_EXERCISE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FOOD -> {
                val binding = ItemFoodHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                FoodViewHolder(binding, onFoodEditClick, onFoodDeleteClick)
            }
            else -> {
                val binding = ItemExerciseHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ExerciseViewHolder(binding, onExerciseEditClick, onExerciseDeleteClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is LogItem.FoodLog -> (holder as FoodViewHolder).bind(item.log, item.isToday)
            is LogItem.ExerciseLog -> (holder as ExerciseViewHolder).bind(item.log, item.isToday)
        }
    }

    fun updateItems(foodLogs: List<com.hienpc.bmiapp.data.model.FoodLogHistoryResponse>, 
                   exerciseLogs: List<com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse>) {
        val items = mutableListOf<LogItem>()
        items.addAll(foodLogs.map { LogItem.FoodLog(it) })
        items.addAll(exerciseLogs.map { LogItem.ExerciseLog(it) })
        
        // Sort by time (most recent first)
        items.sortByDescending { item ->
            when (item) {
                is LogItem.FoodLog -> {
                    try {
                        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        format.parse(item.log.dateEaten)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }
                is LogItem.ExerciseLog -> {
                    try {
                        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        format.parse(item.log.dateExercised)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }
            }
        }
        
        submitList(items)
    }

    class FoodViewHolder(
        private val binding: ItemFoodHistoryBinding,
        private val onEditClick: (com.hienpc.bmiapp.data.model.FoodLogHistoryResponse) -> Unit,
        private val onDeleteClick: (com.hienpc.bmiapp.data.model.FoodLogHistoryResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: com.hienpc.bmiapp.data.model.FoodLogHistoryResponse, isToday: Boolean) {
            binding.textFoodName.text = item.foodName
            binding.textQuantity.text = "${item.quantity} ${item.unit}"
            binding.textCalories.text = "${item.totalCalories} kcal"

            // Format date
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(item.dateEaten)
                if (date != null) {
                    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    binding.textDate.text = outputFormat.format(date)
                } else {
                    binding.textDate.text = item.dateEaten
                }
            } catch (e: Exception) {
                binding.textDate.text = item.dateEaten
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

    class ExerciseViewHolder(
        private val binding: ItemExerciseHistoryBinding,
        private val onEditClick: (com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse) -> Unit,
        private val onDeleteClick: (com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse, isToday: Boolean) {
            binding.textExerciseName.text = item.exerciseName
            binding.textDuration.text = "${item.duration.toInt()} phút"
            binding.textCalories.text = "${item.totalCaloriesBurned} kcal"

            // Format date
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(item.dateExercised)
                if (date != null) {
                    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    binding.textDate.text = outputFormat.format(date)
                } else {
                    binding.textDate.text = item.dateExercised
                }
            } catch (e: Exception) {
                binding.textDate.text = item.dateExercised
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

    private class DiffCallback : DiffUtil.ItemCallback<LogItem>() {
        override fun areItemsTheSame(oldItem: LogItem, newItem: LogItem): Boolean {
            return when {
                oldItem is LogItem.FoodLog && newItem is LogItem.FoodLog -> 
                    oldItem.log.logId == newItem.log.logId
                oldItem is LogItem.ExerciseLog && newItem is LogItem.ExerciseLog -> 
                    oldItem.log.logId == newItem.log.logId
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: LogItem, newItem: LogItem): Boolean {
            return oldItem == newItem
        }
    }
}

