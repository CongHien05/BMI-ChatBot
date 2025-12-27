package com.hienpc.bmiapp.ui.main.history

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.data.model.FoodLogHistoryResponse
import com.hienpc.bmiapp.data.model.FoodResponse
import com.hienpc.bmiapp.databinding.FragmentFoodHistoryBinding
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.viewmodel.LogHistoryViewModel
import com.hienpc.bmiapp.viewmodel.LogViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * FoodHistoryFragment - hiển thị lịch sử các bữa ăn đã log
 */
class FoodHistoryFragment : Fragment() {

    private var _binding: FragmentFoodHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogHistoryViewModel by viewModels()
    private val logViewModel: LogViewModel by viewModels() // To load foods list
    private lateinit var adapter: FoodHistoryAdapter
    private var allFoods: List<FoodResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        
        // Load foods list for edit dialog
        logViewModel.loadFoods()
        
        // Load last 30 days by default
        viewModel.loadFoodHistory()
    }

    private fun setupRecyclerView() {
        adapter = FoodHistoryAdapter(
            onEditClick = { logItem ->
                showEditDialog(logItem)
            },
            onDeleteClick = { logItem ->
                showDeleteConfirmation(logItem)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FoodHistoryFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.foodHistoryState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.textEmpty.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.data.isEmpty()) {
                        binding.recyclerView.visibility = View.GONE
                        binding.textEmpty.visibility = View.VISIBLE
                    } else {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.textEmpty.visibility = View.GONE
                        adapter.updateItems(state.data)
                    }
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.textEmpty.visibility = View.VISIBLE
                    binding.textEmpty.text = state.message
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        })

        viewModel.deleteFoodLogState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), "Đã xóa log thành công", Toast.LENGTH_SHORT).show()
                    viewModel.resetDeleteStates()
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetDeleteStates()
                }
                else -> {}
            }
        })

        viewModel.updateFoodLogState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), "Đã cập nhật log thành công", Toast.LENGTH_SHORT).show()
                    viewModel.resetUpdateStates()
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetUpdateStates()
                }
                else -> {}
            }
        })

        // Observe foods list for edit dialog
        logViewModel.foodsState.observe(viewLifecycleOwner, Observer { state ->
            if (state is UiState.Success) {
                allFoods = state.data
            }
        })
    }

    private fun showDeleteConfirmation(logItem: FoodLogHistoryResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa log này?\n\n${logItem.foodName}\n${logItem.quantity} ${logItem.unit}")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteFoodLog(logItem.logId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditDialog(logItem: FoodLogHistoryResponse) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_food_log, null)
        val foodSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerFood)
        val quantityEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextQuantity)
        val mealTypeSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerMealType)

        // Load foods into spinner
        if (allFoods.isEmpty()) {
            Toast.makeText(requireContext(), "Đang tải danh sách món ăn...", Toast.LENGTH_SHORT).show()
            logViewModel.loadFoods()
            lifecycleScope.launch {
                // Wait for foods to load
                kotlinx.coroutines.delay(500)
                if (allFoods.isNotEmpty()) {
                    setupFoodSpinner(foodSpinner, allFoods, logItem)
                }
            }
        } else {
            setupFoodSpinner(foodSpinner, allFoods, logItem)
        }

        // Set current values - convert from food's unit back to grams for display
        // logItem.quantity is in food's unit (e.g., 2 chén = 2.0), we need to convert to grams
        val currentQuantityInGrams = if (logItem.unit.lowercase().contains("gram") || logItem.unit.lowercase() == "g") {
            // If unit is gram, backend stores quantity in units of 100g, so multiply by 100
            logItem.quantity * 100.0
        } else {
            // Convert from food's unit (portions) to grams
            val gramsPerUnit = when {
                logItem.unit.lowercase().contains("chén") -> 150.0
                logItem.unit.lowercase().contains("tô") -> 300.0
                logItem.unit.lowercase().contains("đĩa") -> 250.0
                logItem.unit.lowercase().contains("ổ") -> 100.0
                else -> 200.0
            }
            logItem.quantity * gramsPerUnit
        }
        quantityEditText.setText(currentQuantityInGrams.toInt().toString())
        
        // Setup meal type spinner
        val mealTypes = arrayOf("Breakfast", "Lunch", "Dinner", "Snack")
        val mealTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mealTypes)
        mealTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mealTypeSpinner.adapter = mealTypeAdapter
        // Set current meal type
        val currentMealType = logItem.mealType ?: "Breakfast"
        val currentMealTypeIndex = mealTypes.indexOfFirst { 
            it.equals(currentMealType, ignoreCase = true) || 
            currentMealType.contains(it, ignoreCase = true)
        }
        if (currentMealTypeIndex >= 0) {
            mealTypeSpinner.setSelection(currentMealTypeIndex)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa log")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val selectedFood = foodSpinner.selectedItem as? FoodResponse
                val quantity = quantityEditText.text.toString().toDoubleOrNull()
                val mealType = mealTypeSpinner.selectedItem as? String

                if (selectedFood == null || quantity == null || quantity <= 0 || mealType == null) {
                    Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Convert quantity from gram to food's unit (same logic as FoodLogFragment)
                val (convertedQuantity, _, _) = if (selectedFood.unit.lowercase().contains("gram") || selectedFood.unit.lowercase() == "g") {
                    Triple(quantity / 100.0, 0, "")
                } else {
                    val gramsPerUnit = when {
                        selectedFood.unit.lowercase().contains("chén") -> 150.0
                        selectedFood.unit.lowercase().contains("tô") -> 300.0
                        selectedFood.unit.lowercase().contains("đĩa") -> 250.0
                        selectedFood.unit.lowercase().contains("ổ") -> 100.0
                        else -> 200.0
                    }
                    Triple(quantity / gramsPerUnit, 0, "")
                }

                viewModel.updateFoodLog(logItem.logId, selectedFood.id, convertedQuantity, mealType)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun setupFoodSpinner(spinner: android.widget.Spinner, foods: List<FoodResponse>, currentLog: FoodLogHistoryResponse) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, foods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        // Select current food
        val currentFoodIndex = foods.indexOfFirst { it.id == currentLog.foodId }
        if (currentFoodIndex >= 0) {
            spinner.setSelection(currentFoodIndex)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

