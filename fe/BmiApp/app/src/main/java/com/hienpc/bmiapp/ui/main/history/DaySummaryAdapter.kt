package com.hienpc.bmiapp.ui.main.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hienpc.bmiapp.databinding.ItemDaySummaryBinding
import com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse
import com.hienpc.bmiapp.data.model.FoodLogHistoryResponse

enum class SummaryType {
    FOOD, EXERCISE
}

data class DaySummaryItem(
    val type: SummaryType,
    val title: String,
    val icon: String,
    val totalCalories: Int,
    val itemCount: Int,
    val itemUnit: String,
    val logs: List<LogItem>,
    val isExpanded: Boolean = false
)

/**
 * Adapter for displaying day summary cards (Food and Exercise)
 */
class DaySummaryAdapter(
    private val onFoodEditClick: (FoodLogHistoryResponse) -> Unit,
    private val onFoodDeleteClick: (FoodLogHistoryResponse) -> Unit,
    private val onExerciseEditClick: (ExerciseLogHistoryResponse) -> Unit,
    private val onExerciseDeleteClick: (ExerciseLogHistoryResponse) -> Unit
) : ListAdapter<DaySummaryItem, DaySummaryAdapter.ViewHolder>(DiffCallback()) {

    private val expandedItems = mutableSetOf<SummaryType>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDaySummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(
            binding, 
            onFoodEditClick, 
            onFoodDeleteClick, 
            onExerciseEditClick, 
            onExerciseDeleteClick,
            onExpandToggle = { type ->
                if (expandedItems.contains(type)) {
                    expandedItems.remove(type)
                } else {
                    expandedItems.add(type)
                }
                // Reload summary to update expanded state
                // This will be handled by the fragment calling updateSummary again
            }
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateSummary(summary: DaySummary) {
        val items = listOf(
            DaySummaryItem(
                type = SummaryType.FOOD,
                title = "Bữa ăn",
                icon = "🍽️",
                totalCalories = summary.totalFoodCalories,
                itemCount = summary.foodCount,
                itemUnit = "món",
                logs = summary.foodLogs.map { LogItem.FoodLog(it) },
                isExpanded = expandedItems.contains(SummaryType.FOOD)
            ),
            DaySummaryItem(
                type = SummaryType.EXERCISE,
                title = "Bài tập",
                icon = "💪",
                totalCalories = summary.totalExerciseCalories,
                itemCount = summary.exerciseCount,
                itemUnit = "bài",
                logs = summary.exerciseLogs.map { LogItem.ExerciseLog(it) },
                isExpanded = expandedItems.contains(SummaryType.EXERCISE)
            )
        )
        submitList(items)
    }

    class ViewHolder(
        private val binding: ItemDaySummaryBinding,
        private val onFoodEditClick: (FoodLogHistoryResponse) -> Unit,
        private val onFoodDeleteClick: (FoodLogHistoryResponse) -> Unit,
        private val onExerciseEditClick: (ExerciseLogHistoryResponse) -> Unit,
        private val onExerciseDeleteClick: (ExerciseLogHistoryResponse) -> Unit,
        private val onExpandToggle: (SummaryType) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentItem: DaySummaryItem? = null
        private var isExpanded: Boolean = false
        private lateinit var logsAdapter: DailyLogsAdapter

        fun bind(item: DaySummaryItem) {
            currentItem = item
            isExpanded = item.isExpanded

            binding.textIcon.text = item.icon
            binding.textTitle.text = item.title
            binding.textSubtitle.text = "${item.itemCount} ${item.itemUnit}"
            binding.textTotalCalories.text = item.totalCalories.toString()
            binding.textItemCount.text = item.itemCount.toString()

            // Set color based on type
            val colorRes = if (item.type == SummaryType.FOOD) {
                com.hienpc.bmiapp.R.color.gradient_start_food
            } else {
                com.hienpc.bmiapp.R.color.gradient_start_exercise
            }
            val color = androidx.core.content.ContextCompat.getColor(binding.root.context, colorRes)
            binding.textTotalCalories.setTextColor(color)
            binding.textItemCount.setTextColor(color)

            // Setup logs RecyclerView
            if (!::logsAdapter.isInitialized) {
                logsAdapter = DailyLogsAdapter(
                    onFoodEditClick = onFoodEditClick,
                    onFoodDeleteClick = onFoodDeleteClick,
                    onExerciseEditClick = onExerciseEditClick,
                    onExerciseDeleteClick = onExerciseDeleteClick
                )
                binding.recyclerLogs.apply {
                    layoutManager = LinearLayoutManager(binding.root.context)
                    adapter = logsAdapter
                }
            }

            // Update logs
            val foodLogs = item.logs.filterIsInstance<LogItem.FoodLog>().map { it.log }
            val exerciseLogs = item.logs.filterIsInstance<LogItem.ExerciseLog>().map { it.log }
            logsAdapter.updateItems(foodLogs, exerciseLogs)

            // Expand/Collapse
            updateExpandState(item.isExpanded)

            binding.root.setOnClickListener {
                isExpanded = !isExpanded
                onExpandToggle(item.type)
                // Update UI immediately
                updateExpandState(isExpanded)
            }
        }

        private fun updateExpandState(isExpanded: Boolean) {
            if (isExpanded) {
                binding.recyclerLogs.visibility = View.VISIBLE
                binding.imageExpand.rotation = 180f
            } else {
                binding.recyclerLogs.visibility = View.GONE
                binding.imageExpand.rotation = 0f
            }
        }
    }


    private class DiffCallback : DiffUtil.ItemCallback<DaySummaryItem>() {
        override fun areItemsTheSame(oldItem: DaySummaryItem, newItem: DaySummaryItem): Boolean {
            return oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: DaySummaryItem, newItem: DaySummaryItem): Boolean {
            // Compare all fields except isExpanded (which is managed separately)
            return oldItem.type == newItem.type &&
                   oldItem.title == newItem.title &&
                   oldItem.icon == newItem.icon &&
                   oldItem.totalCalories == newItem.totalCalories &&
                   oldItem.itemCount == newItem.itemCount &&
                   oldItem.itemUnit == newItem.itemUnit &&
                   oldItem.logs.size == newItem.logs.size
        }
    }
}

