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

class FoodLogFragment : Fragment() {

    private var _binding: FragmentFoodLogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogViewModel by viewModels()

    private var foods: List<FoodResponse> = emptyList()
    private var allFoods: List<FoodResponse> = emptyList() // Store all foods for filtering
    private lateinit var recommendationAdapter: RecommendationAdapter
    private var searchJob: Job? = null
    private var selectedFood: FoodResponse? = null

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
        setupFoodAutoComplete()
        setupMealTypeChips()
        setupListeners()
        observeViewModel()

        viewModel.loadFoods() // Load all foods initially
        viewModel.loadFoodRecommendations(10)
    }
    
    private fun setupRecommendationsRecyclerView() {
        recommendationAdapter = RecommendationAdapter(emptyList()) { recommendedItem ->
            // When user clicks a recommendation, auto-fill the form AND focus on quantity
            val food = allFoods.find { it.name == recommendedItem.name }
            if (food != null) {
                binding.autoCompleteFood.setText(food.name, false)
                selectedFood = food
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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
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
        binding.autoCompleteFood.setOnItemClickListener { _, _, position, _ ->
            selectedFood = foods[position]
        }
    }

    private fun setupMealTypeChips() {
        // ChipGroup is already configured in XML with singleSelection="true"
        // Default selection is chipBreakfast (set in XML)
        // No additional setup needed
    }

    private fun setupListeners() {
        binding.buttonLogFood.setOnClickListener {
            val food = selectedFood ?: run {
                // Try to find food by name if not selected
                val foodName = binding.autoCompleteFood.text.toString().trim()
                allFoods.find { it.name.equals(foodName, ignoreCase = true) }
            }
            
            if (food == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn món ăn", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

            viewModel.logFood(food.id, quantity, mealType)
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
                    recommendationAdapter.updateItems(data.items)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


