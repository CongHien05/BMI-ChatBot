package com.hienpc.bmiapp.ui.main.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hienpc.bmiapp.data.model.ExerciseResponse
import com.hienpc.bmiapp.databinding.ItemFavoriteExerciseBinding

/**
 * Adapter for displaying favorite exercises
 */
class FavoriteExerciseAdapter(
    private var items: List<ExerciseResponse>,
    private val onItemClick: (ExerciseResponse) -> Unit,
    private val onToggleFavorite: (ExerciseResponse, Boolean) -> Unit,
    private val onLongClick: ((ExerciseResponse) -> Unit)? = null
) : RecyclerView.Adapter<FavoriteExerciseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoriteExerciseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ExerciseResponse>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    fun getItems(): List<ExerciseResponse> = items
    
    fun getItem(position: Int): ExerciseResponse? {
        return if (position in 0 until items.size) items[position] else null
    }

    inner class ViewHolder(
        private val binding: ItemFavoriteExerciseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: ExerciseResponse) {
            binding.textExerciseName.text = exercise.name
            binding.textCalories.text = "${exercise.caloriesBurnedPerHour} kcal/giờ"
            
            // Star icon is always filled (since these are favorites)
            binding.buttonToggleFavorite.setImageResource(android.R.drawable.btn_star_big_on)
            binding.buttonToggleFavorite.contentDescription = "Bỏ yêu thích"
            
            // Click on card to select exercise
            binding.root.setOnClickListener {
                onItemClick(exercise)
            }
            
            // Long press on card for options (if callback provided)
            binding.root.setOnLongClickListener {
                onLongClick?.invoke(exercise)
                true
            }
            
            // Click on star to remove from favorites
            binding.buttonToggleFavorite.setOnClickListener {
                onToggleFavorite(exercise, true) // true = currently favorite, so remove
            }
        }
    }
}

