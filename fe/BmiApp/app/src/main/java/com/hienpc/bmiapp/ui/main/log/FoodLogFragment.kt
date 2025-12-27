package com.hienpc.bmiapp.ui.main.log

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Filterable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hienpc.bmiapp.data.model.FoodResponse
import com.hienpc.bmiapp.databinding.FragmentFoodLogBinding
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.utils.show
import com.hienpc.bmiapp.utils.hide
import com.hienpc.bmiapp.viewmodel.LogViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.hienpc.bmiapp.R


class FoodLogFragment : Fragment() {

    private var _binding: FragmentFoodLogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogViewModel by viewModels()

    private var foods: List<FoodResponse> = emptyList()
    private var allFoods: List<FoodResponse> = emptyList() // Store all foods for filtering
    private var favoriteFoods: List<FoodResponse> = emptyList()
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var favoriteAdapter: FavoriteFoodAdapter
    private var searchJob: Job? = null
    private var selectedFood: FoodResponse? = null
    private var isShowingFavorites: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecommendationsRecyclerView()
        setupFavoritesRecyclerView()
        setupFoodAutoComplete()
        setupMealTypeChips()
        setupToggleFavoritesButton()
        setupHistoryButton()
        setupListeners()
        observeViewModel()

        viewModel.loadFoods() // Load all foods initially
        viewModel.loadFavoriteFoods() // Load favorites
        viewModel.loadFoodRecommendations(10)
    }
    
    private var isRecommendationsExpanded = false
    
    private fun setupRecommendationsRecyclerView() {
        recommendationAdapter = RecommendationAdapter(emptyList()) { recommendedItem ->
            // When user clicks a recommendation, auto-fill the form AND focus on quantity
            val food = allFoods.find { it.name == recommendedItem.name }
            if (food != null) {
                binding.autoCompleteFood.setText(food.name, false)
                selectedFood = food
                // Update calories display
                updateCaloriesDisplay()
                // Focus on quantity input for better flow
                binding.editTextQuantity.requestFocus()
                // Show keyboard
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editTextQuantity, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        
        binding.recyclerRecommendations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendationAdapter
        }
        
        // Setup expand/collapse button
        binding.buttonShowMoreRecommendations.setOnClickListener {
            isRecommendationsExpanded = !isRecommendationsExpanded
            updateRecommendationsDisplay()
        }
    }
    
    private var allRecommendations: List<com.hienpc.bmiapp.data.model.RecommendationItem> = emptyList()
    private var isSelectingFromDropdown = false // Flag to prevent resetting selectedFood when selecting from dropdown
    
    private fun updateRecommendationsDisplay() {
        val allItems = allRecommendations
        if (allItems.size <= 2) {
            // If 2 or fewer items, hide expand button
            binding.buttonShowMoreRecommendations.visibility = View.GONE
            recommendationAdapter.updateItems(allItems)
        } else {
            // Show expand button
            binding.buttonShowMoreRecommendations.visibility = View.VISIBLE
            if (isRecommendationsExpanded) {
                // Show all items
                recommendationAdapter.updateItems(allItems)
                binding.buttonShowMoreRecommendations.text = "Thu gọn"
            } else {
                // Show only first 2 items
                recommendationAdapter.updateItems(allItems.take(2))
                binding.buttonShowMoreRecommendations.text = "Xem thêm ${allItems.size - 2} món"
            }
        }
    }
    
    private fun setupFoodAutoComplete() {
        // Create adapter with custom dropdown layout
        val adapter = object : ArrayAdapter<FoodResponse>(
            requireContext(),
            R.layout.item_dropdown_food
        ) {
            override fun getCount(): Int = foods.size
            override fun getItem(position: Int): FoodResponse = foods[position]
            override fun getItemId(position: Int): Long = foods[position].id.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_dropdown_food, parent, false)
                
                val textItemName = view.findViewById<android.widget.TextView>(R.id.textItemName)
                textItemName.text = foods[position].name
                
                return view
            }
        }
        
        binding.autoCompleteFood.setAdapter(adapter)
        // Disable threshold to show dropdown immediately when there are results
        binding.autoCompleteFood.threshold = 1
        
        // Handle text change with debounce for search
        binding.autoCompleteFood.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Only reset selectedFood when user types manually (not when selecting from dropdown)
                if (!isSelectingFromDropdown) {
                    android.util.Log.d("FoodLog", "User typing, resetting selectedFood")
                    selectedFood = null
                    updateCaloriesDisplay() // Update to hide calories info
                } else {
                    android.util.Log.d("FoodLog", "Selecting from dropdown, keeping selectedFood")
                }
            }
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim()
                
                // Cancel previous search
                searchJob?.cancel()
                
                // Debounce search (wait 500ms after user stops typing)
                searchJob = lifecycleScope.launch {
                    delay(500)
                    if (query.isNullOrEmpty() || query.length < 2) {
                        // If query is too short, show all foods
                        viewModel.loadFoods(null)
                    } else {
                        // Search with query
                        viewModel.loadFoods(query)
                    }
                }
            }
        })
        
        // Handle item selection
        binding.autoCompleteFood.setOnItemClickListener { parent, _, position, _ ->
            android.util.Log.d("FoodLog", "=== OnItemClickListener triggered ===")
            android.util.Log.d("FoodLog", "Position: $position")
            isSelectingFromDropdown = true // Set flag to prevent TextWatcher from resetting selectedFood
            val adapter = parent.adapter as? ArrayAdapter<FoodResponse>
            val selectedItem = adapter?.getItem(position)
            android.util.Log.d("FoodLog", "Selected item: ${selectedItem?.name}")
            if (selectedItem != null) {
                selectedFood = selectedItem
                android.util.Log.d("FoodLog", "selectedFood set to: ${selectedFood?.name}")
                android.util.Log.d("FoodLog", "Food caloriesPerUnit: ${selectedItem.caloriesPerUnit}, unit: ${selectedItem.unit}")
                // Update text to show selected item name (this will trigger TextWatcher, but flag prevents reset)
                binding.autoCompleteFood.setText(selectedItem.name, false)
                // Reset flag after a short delay to allow TextWatcher to process
                binding.autoCompleteFood.postDelayed({
                    isSelectingFromDropdown = false
                    android.util.Log.d("FoodLog", "Reset isSelectingFromDropdown flag")
                }, 100)
                // Dismiss dropdown
                binding.autoCompleteFood.dismissDropDown()
                // Update calories display
                android.util.Log.d("FoodLog", "Calling updateCaloriesDisplay()...")
                updateCaloriesDisplay()
            } else {
                android.util.Log.e("FoodLog", "ERROR: selectedItem is null!")
                isSelectingFromDropdown = false
            }
        }
        
        // Listen to quantity changes for realtime calories calculation
        binding.editTextQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateCaloriesDisplay()
            }
        })
    }

    private fun setupFavoritesRecyclerView() {
        favoriteAdapter = FavoriteFoodAdapter(emptyList(),
            onItemClick = { food ->
                // When user clicks a favorite, switch to form view and select the food
                toggleFavoritesView(false)
                binding.autoCompleteFood.setText(food.name, false)
                selectedFood = food
                binding.editTextQuantity.requestFocus()
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editTextQuantity, InputMethodManager.SHOW_IMPLICIT)
            },
            onToggleFavorite = { food, isCurrentlyFavorite ->
                viewModel.toggleFavoriteFood(food.id, isCurrentlyFavorite)
            }
        )
        
        binding.recyclerFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
        }
    }

    private fun setupToggleFavoritesButton() {
        binding.buttonToggleFavorites.setOnClickListener {
            isShowingFavorites = !isShowingFavorites
            toggleFavoritesView(isShowingFavorites)
        }
    }

    private fun setupHistoryButton() {
        binding.buttonHistory.setOnClickListener {
            // Navigate to LogHistoryFragment
            val historyFragment = com.hienpc.bmiapp.ui.main.history.LogHistoryFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, historyFragment)
                .addToBackStack("LogHistory")
                .commit()
        }
    }

    private fun toggleFavoritesView(showFavorites: Boolean) {
        isShowingFavorites = showFavorites
        if (showFavorites) {
            binding.cardForm.visibility = View.GONE
            binding.cardFavorites.visibility = View.VISIBLE
            binding.buttonToggleFavorites.text = "Tất cả"
            binding.buttonToggleFavorites.chipIcon = requireContext().getDrawable(android.R.drawable.btn_star_big_on)
            viewModel.loadFavoriteFoods() // Refresh favorites
        } else {
            binding.cardForm.visibility = View.VISIBLE
            binding.cardFavorites.visibility = View.GONE
            binding.buttonToggleFavorites.text = "⭐ Yêu thích"
            binding.buttonToggleFavorites.chipIcon = requireContext().getDrawable(android.R.drawable.btn_star_big_off)
        }
    }

    private fun updateCaloriesDisplay() {
        android.util.Log.d("FoodLog", "=== updateCaloriesDisplay() called ===")
        val food = selectedFood
        val quantityText = binding.editTextQuantity.text.toString()
        val quantity = quantityText.toDoubleOrNull()
        
        android.util.Log.d("FoodLog", "selectedFood: ${food?.name ?: "null"}")
        android.util.Log.d("FoodLog", "quantityText: '$quantityText', quantity: $quantity")
        
        if (food != null) {
            if (quantity != null && quantity > 0) {
                // Calculate calories based on unit type
                val calories = if (food.unit.lowercase().contains("gram") || food.unit.lowercase() == "g") {
                    // Unit is in grams, caloriesPerUnit is per 100g
                    (food.caloriesPerUnit * quantity / 100.0).toInt()
                } else {
                    // For portion-based units, estimate conversion
                    val gramsPerUnit = when {
                        food.unit.lowercase().contains("chén") -> 150.0
                        food.unit.lowercase().contains("tô") -> 300.0
                        food.unit.lowercase().contains("đĩa") -> 250.0
                        food.unit.lowercase().contains("ổ") -> 100.0
                        else -> 200.0 // default estimate
                    }
                    (food.caloriesPerUnit * quantity / gramsPerUnit).toInt()
                }
                android.util.Log.d("FoodLog", "Calculated calories: $calories kcal")
                binding.textCaloriesInfo.text = "Tổng calories: $calories kcal"
            } else {
                // Show calories per unit if no quantity entered yet
                val displayText = "${food.caloriesPerUnit} kcal / ${food.unit}"
                android.util.Log.d("FoodLog", "No quantity, showing per unit: $displayText")
                binding.textCaloriesInfo.text = displayText
            }
            binding.layoutCaloriesInfo.visibility = View.VISIBLE
            android.util.Log.d("FoodLog", "Calories info layout set to VISIBLE")
        } else {
            android.util.Log.w("FoodLog", "WARNING: food is null, hiding calories info")
            binding.layoutCaloriesInfo.visibility = View.GONE
        }
    }

    private fun setupMealTypeChips() {
        // ChipGroup is already configured in XML with singleSelection="true"
        // Default selection is chipBreakfast (set in XML)
        // No additional setup needed
    }

    private fun setupListeners() {
        binding.buttonLogFood.setOnClickListener {
            android.util.Log.d("FoodLog", "=== buttonLogFood clicked ===")
            android.util.Log.d("FoodLog", "selectedFood: ${selectedFood?.name ?: "null"}")
            val food = selectedFood ?: run {
                // Try to find food by name if not selected
                val foodName = binding.autoCompleteFood.text.toString().trim()
                android.util.Log.d("FoodLog", "selectedFood is null, trying to find by name: '$foodName'")
                val found = allFoods.find { it.name.equals(foodName, ignoreCase = true) }
                android.util.Log.d("FoodLog", "Found food by name: ${found?.name ?: "null"}")
                found
            }
            
            if (food == null) {
                android.util.Log.e("FoodLog", "ERROR: Cannot find food!")
                Toast.makeText(requireContext(), "Vui lòng chọn món ăn", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            android.util.Log.d("FoodLog", "Using food: ${food.name}, caloriesPerUnit: ${food.caloriesPerUnit}, unit: ${food.unit}")

            val quantityText = binding.editTextQuantity.text.toString()
            val quantity = quantityText.toDoubleOrNull()
            if (quantity == null || quantity <= 0) {
                Toast.makeText(requireContext(), "Số lượng phải > 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get selected meal type from ChipGroup
            val selectedChipId = binding.chipGroupMealType.checkedChipId
            if (selectedChipId == View.NO_ID) {
                Toast.makeText(requireContext(), "Vui lòng chọn bữa ăn", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val mealType = when (selectedChipId) {
                binding.chipBreakfast.id -> "Breakfast"
                binding.chipLunch.id -> "Lunch"
                binding.chipDinner.id -> "Dinner"
                binding.chipSnack.id -> "Snack"
                else -> "Breakfast"
            }
            
            // Calculate calories and convert quantity to food's unit
            // User always inputs in GRAMS, but we need to convert to food's unit for API
            val (convertedQuantity, calories, quantityDisplayText) = if (food.unit.lowercase().contains("gram") || food.unit.lowercase() == "g") {
                // Food unit is already in grams, caloriesPerUnit is per 100g
                val calories = (food.caloriesPerUnit * quantity / 100.0).toInt()
                Triple(quantity / 100.0, calories, "${quantity.toInt()} gram")
            } else {
                // Food unit is portion-based (chén, tô, etc.), caloriesPerUnit is per unit
                // Convert grams to food's unit
                val gramsPerUnit = when {
                    food.unit.lowercase().contains("chén") -> 150.0
                    food.unit.lowercase().contains("tô") -> 300.0
                    food.unit.lowercase().contains("đĩa") -> 250.0
                    food.unit.lowercase().contains("ổ") -> 100.0
                    else -> 200.0 // default estimate
                }
                val convertedQty = quantity / gramsPerUnit
                val calories = (food.caloriesPerUnit * convertedQty).toInt()
                Triple(convertedQty, calories, "${quantity.toInt()} gram (≈ ${String.format("%.2f", convertedQty)} ${food.unit})")
            }
            
            android.util.Log.d("FoodLog", "Quantity conversion: ${quantity}g -> ${String.format("%.4f", convertedQuantity)} ${food.unit}")
            android.util.Log.d("FoodLog", "Calories: $calories kcal")
            
            val mealTypeText = when (mealType) {
                "Breakfast" -> "bữa sáng"
                "Lunch" -> "bữa trưa"
                "Dinner" -> "bữa tối"
                "Snack" -> "bữa xế"
                else -> "bữa ăn"
            }
            
            // Show confirmation dialog - send converted quantity to API
            showConfirmationDialog(food.name, quantityDisplayText, calories, mealTypeText, food.id, convertedQuantity, mealType)
        }
    }

    private fun observeViewModel() {
        viewModel.foodsState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonLogFood.isEnabled = false
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonLogFood.isEnabled = true

                    foods = state.data
                    // Store all foods for recommendations and fallback
                    if (allFoods.isEmpty()) {
                        allFoods = foods
                    }
                    
                    // Create new adapter with custom dropdown layout
                    val adapter = object : ArrayAdapter<FoodResponse>(
                        requireContext(),
                        R.layout.item_dropdown_food
                    ) {
                        override fun getCount(): Int = foods.size
                        override fun getItem(position: Int): FoodResponse = foods[position]
                        override fun getItemId(position: Int): Long = foods[position].id.toLong()
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = convertView ?: LayoutInflater.from(context)
                                .inflate(R.layout.item_dropdown_food, parent, false)
                            
                            val textItemName = view.findViewById<android.widget.TextView>(R.id.textItemName)
                            textItemName.text = foods[position].name
                            
                            return view
                        }
                    }
                    binding.autoCompleteFood.setAdapter(adapter)
                    android.util.Log.d("FoodLog", "Adapter set in observeViewModel, foods count: ${foods.size}")
                    
                    // Re-set item click listener (in case it was lost)
                    binding.autoCompleteFood.setOnItemClickListener { parent, _, position, _ ->
                        android.util.Log.d("FoodLog", "=== OnItemClickListener triggered (in observeViewModel) ===")
                        android.util.Log.d("FoodLog", "Position: $position")
                        isSelectingFromDropdown = true // Set flag to prevent TextWatcher from resetting selectedFood
                        val adapter = parent.adapter as? ArrayAdapter<FoodResponse>
                        val selectedItem = adapter?.getItem(position)
                        android.util.Log.d("FoodLog", "Selected item: ${selectedItem?.name}")
                        if (selectedItem != null) {
                            selectedFood = selectedItem
                            android.util.Log.d("FoodLog", "selectedFood set to: ${selectedFood?.name}")
                            // Update text to show selected item name (this will trigger TextWatcher, but flag prevents reset)
                            binding.autoCompleteFood.setText(selectedItem.name, false)
                            // Reset flag after a short delay to allow TextWatcher to process
                            binding.autoCompleteFood.postDelayed({
                                isSelectingFromDropdown = false
                                android.util.Log.d("FoodLog", "Reset isSelectingFromDropdown flag (in observeViewModel)")
                            }, 100)
                            // Dismiss dropdown
                            binding.autoCompleteFood.dismissDropDown()
                            // Update calories display
                            android.util.Log.d("FoodLog", "Calling updateCaloriesDisplay() from observeViewModel...")
                            updateCaloriesDisplay()
                        } else {
                            android.util.Log.e("FoodLog", "ERROR: selectedItem is null in observeViewModel!")
                            isSelectingFromDropdown = false
                        }
                    }
                    
                    // Update calories display if food is already selected
                    if (selectedFood != null) {
                        updateCaloriesDisplay()
                    }
                    
                    // Show dropdown if there are results and text field has focus
                    if (foods.isNotEmpty() && binding.autoCompleteFood.hasFocus()) {
                        binding.autoCompleteFood.showDropDown()
                    }
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonLogFood.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        })

        viewModel.logFoodState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Loading -> binding.buttonLogFood.isEnabled = false
                is UiState.Success -> {
                    binding.buttonLogFood.isEnabled = true
                    Toast.makeText(requireContext(), "Log món ăn thành công", Toast.LENGTH_SHORT)
                        .show()
                    binding.editTextQuantity.text?.clear()
                    viewModel.resetLogStates()
                }
                is UiState.Error -> {
                    binding.buttonLogFood.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetLogStates()
                }
                else -> Unit
            }
        })
        
        // Observe food recommendations
        viewModel.foodRecommendationsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val data = state.data
                    binding.textRecommendationExplanation.text = data.explanation
                    // Store all recommendations
                    allRecommendations = data.items
                    // Update display (show only 1-2 items initially)
                    updateRecommendationsDisplay()
                    binding.cardRecommendations.show()
                }
                is UiState.Error -> {
                    binding.cardRecommendations.hide()
                }
                is UiState.Loading -> {
                    binding.textRecommendationExplanation.text = "⏳ Đang tải gợi ý AI..."
                    binding.cardRecommendations.show()
                }
                else -> {}
            }
        }
        
        // Observe favorite foods
        viewModel.favoriteFoodsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    favoriteFoods = state.data
                    favoriteAdapter.updateItems(favoriteFoods)
                }
                is UiState.Error -> {
                    // Silent fail for favorites
                }
                else -> {}
            }
        }
        
        // Observe toggle favorite state
        viewModel.toggleFavoriteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    // Favorite toggled successfully, list will auto-refresh
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun showConfirmationDialog(
        foodName: String,
        quantityDisplayText: String, // Display text (e.g., "200 gram (≈ 1.33 chén)")
        calories: Int,
        mealTypeText: String,
        foodId: Int,
        quantityValue: Double, // Converted quantity to send to API
        mealType: String
    ) {
        android.util.Log.d("FoodLog", "showConfirmationDialog: quantityValue=$quantityValue, calories=$calories")
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận ghi lại bữa ăn")
            .setMessage(
                "Bạn đang nạp vào $calories kcal cho $mealTypeText.\n\n" +
                "Món: $foodName\n" +
                "Số lượng: $quantityDisplayText"
            )
            .setPositiveButton("Xác nhận") { _, _ ->
                android.util.Log.d("FoodLog", "Sending to API: foodId=$foodId, quantity=$quantityValue, mealType=$mealType")
                viewModel.logFood(foodId, quantityValue, mealType)
            }
            .setNegativeButton("Hủy", null)
            .setCancelable(true)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


