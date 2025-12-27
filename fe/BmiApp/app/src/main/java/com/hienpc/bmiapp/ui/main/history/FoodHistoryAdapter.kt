package com.hienpc.bmiapp.ui.main.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hienpc.bmiapp.data.model.FoodLogHistoryResponse
import com.hienpc.bmiapp.databinding.ItemFoodHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying food log history items
 */
class FoodHistoryAdapter(
    private val onEditClick: (FoodLogHistoryResponse) -> Unit,
    private val onDeleteClick: (FoodLogHistoryResponse) -> Unit
) : ListAdapter<FoodLogHistoryResponse, FoodHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateItems(newItems: List<FoodLogHistoryResponse>) {
        submitList(newItems)
    }

    class ViewHolder(
        private val binding: ItemFoodHistoryBinding,
        private val onEditClick: (FoodLogHistoryResponse) -> Unit,
        private val onDeleteClick: (FoodLogHistoryResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FoodLogHistoryResponse) {
            binding.textFoodName.text = item.foodName
            binding.textQuantity.text = "${item.quantity} ${item.unit}"
            binding.textCalories.text = "${item.totalCalories} kcal"

            // Format date (API 24+ compatible)
            try {
                // Parse ISO datetime format: "2025-12-14T12:30:00"
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(item.dateEaten)
                if (date != null) {
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    binding.textDate.text = outputFormat.format(date)
                } else {
                    binding.textDate.text = item.dateEaten
                }
            } catch (e: Exception) {
                binding.textDate.text = item.dateEaten
            }

            // Check if log is from today (can edit/delete)
            val isToday = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(item.dateEaten)
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

    private class DiffCallback : DiffUtil.ItemCallback<FoodLogHistoryResponse>() {
        override fun areItemsTheSame(
            oldItem: FoodLogHistoryResponse,
            newItem: FoodLogHistoryResponse
        ): Boolean = oldItem.logId == newItem.logId

        override fun areContentsTheSame(
            oldItem: FoodLogHistoryResponse,
            newItem: FoodLogHistoryResponse
        ): Boolean = oldItem == newItem
    }
}

