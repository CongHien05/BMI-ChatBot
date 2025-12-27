package com.hienpc.bmiapp.ui.main.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hienpc.bmiapp.databinding.ItemDayBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying calendar days in horizontal RecyclerView
 */
class DayAdapter(
    private val onDayClick: (DayItem) -> Unit
) : ListAdapter<DayItem, DayAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onDayClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateItems(newItems: List<DayItem>) {
        submitList(newItems)
    }

    class ViewHolder(
        private val binding: ItemDayBinding,
        private val onDayClick: (DayItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DayItem) {
            binding.textDayOfMonth.text = item.dayOfMonth.toString()
            binding.textDayOfWeek.text = item.dayOfWeek

            // Highlight today
            if (item.isToday) {
                binding.cardDay.setCardBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(binding.root.context, com.hienpc.bmiapp.R.color.gradient_start_food)
                )
                binding.textDayOfMonth.setTextColor(
                    androidx.core.content.ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.textDayOfWeek.setTextColor(
                    androidx.core.content.ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
            } else {
                binding.cardDay.setCardBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.textDayOfMonth.setTextColor(
                    androidx.core.content.ContextCompat.getColor(binding.root.context, com.hienpc.bmiapp.R.color.text_primary)
                )
                binding.textDayOfWeek.setTextColor(
                    androidx.core.content.ContextCompat.getColor(binding.root.context, com.hienpc.bmiapp.R.color.text_secondary)
                )
            }

            // Highlight selected day
            if (item.isSelected) {
                binding.cardDay.strokeWidth = 3
                binding.cardDay.strokeColor = androidx.core.content.ContextCompat.getColor(
                    binding.root.context,
                    com.hienpc.bmiapp.R.color.gradient_start_food
                )
            } else {
                binding.cardDay.strokeWidth = 1
                binding.cardDay.strokeColor = androidx.core.content.ContextCompat.getColor(
                    binding.root.context,
                    android.R.color.darker_gray
                )
            }

            binding.root.setOnClickListener {
                onDayClick(item)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DayItem>() {
        override fun areItemsTheSame(oldItem: DayItem, newItem: DayItem): Boolean {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return format.format(oldItem.date) == format.format(newItem.date)
        }

        override fun areContentsTheSame(oldItem: DayItem, newItem: DayItem): Boolean {
            return oldItem == newItem
        }
    }
}

