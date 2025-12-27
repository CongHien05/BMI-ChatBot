package com.hienpc.bmiapp.ui.main.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hienpc.bmiapp.data.model.FoodResponse
import com.hienpc.bmiapp.databinding.ItemFavoriteFoodBinding

/**
 * Adapter for displaying favorite foods
 */
class FavoriteFoodAdapter(
    private var items: List<FoodResponse>,
    private val onItemClick: (FoodResponse) -> Unit,
    private val onToggleFavorite: (FoodResponse, Boolean) -> Unit
) : RecyclerView.Adapter<FavoriteFoodAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoriteFoodBinding.inflate(
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

    fun updateItems(newItems: List<FoodResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemFavoriteFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodResponse) {
            binding.textFoodName.text = food.name
            binding.textCalories.text = "${food.caloriesPerUnit} kcal / ${food.unit}"
            
            // Star icon is always filled (since these are favorites)
            binding.buttonToggleFavorite.setImageResource(android.R.drawable.btn_star_big_on)
            binding.buttonToggleFavorite.contentDescription = "Bỏ yêu thích"
            
            // Click on card to select food
            binding.root.setOnClickListener {
                onItemClick(food)
            }
            
            // Click on star to remove from favorites
            binding.buttonToggleFavorite.setOnClickListener {
                onToggleFavorite(food, true) // true = currently favorite, so remove
            }
        }
    }
}

