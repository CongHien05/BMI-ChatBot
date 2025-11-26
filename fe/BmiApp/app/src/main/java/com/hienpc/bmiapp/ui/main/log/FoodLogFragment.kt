package com.hienpc.bmiapp.ui.main.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.hienpc.bmiapp.data.model.FoodResponse
import com.hienpc.bmiapp.databinding.FragmentFoodLogBinding
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.viewmodel.LogViewModel

class FoodLogFragment : Fragment() {

    private var _binding: FragmentFoodLogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogViewModel by viewModels()

    private var foods: List<FoodResponse> = emptyList()

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

        setupMealTypeSpinner()
        setupListeners()
        observeViewModel()

        viewModel.loadFoods()
    }

    private fun setupMealTypeSpinner() {
        val items = listOf("Breakfast", "Lunch", "Dinner", "Snack")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMealType.adapter = adapter
    }

    private fun setupListeners() {
        binding.buttonLogFood.setOnClickListener {
            val selectedPosition = binding.spinnerFood.selectedItemPosition
            if (selectedPosition == -1 || foods.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn món ăn", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedFood = foods[selectedPosition]

            val quantityText = binding.editTextQuantity.text.toString()
            val quantity = quantityText.toDoubleOrNull()
            if (quantity == null || quantity <= 0) {
                Toast.makeText(requireContext(), "Số lượng phải > 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mealType = binding.spinnerMealType.selectedItem as String

            viewModel.logFood(selectedFood.id, quantity, mealType)
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
                    val names = foods.map { it.name }
                    val adapter =
                        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerFood.adapter = adapter
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


