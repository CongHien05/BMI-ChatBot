package com.hienpc.bmiapp.ui.main.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.hienpc.bmiapp.databinding.FragmentLogHistoryBinding
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.viewmodel.LogHistoryViewModel
import com.hienpc.bmiapp.viewmodel.LogViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * LogHistoryFragment - màn hình xem lịch sử logs với calendar view
 */
class LogHistoryFragment : Fragment() {

    private var _binding: FragmentLogHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogHistoryViewModel by viewModels()
    private val logViewModel: LogViewModel by viewModels()
    
    private lateinit var dayAdapter: DayAdapter
    private lateinit var summaryAdapter: DaySummaryAdapter
    
    private var selectedDate: Date? = null
    private var allFoods: List<com.hienpc.bmiapp.data.model.FoodResponse> = emptyList()
    private var allExercises: List<com.hienpc.bmiapp.data.model.ExerciseResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendarRecyclerView()
        setupLogsRecyclerView()
        observeViewModel()
        
        // Load foods and exercises for edit dialogs
        logViewModel.loadFoods()
        logViewModel.loadExercises()
        
        // Initialize with current month
        setupCurrentMonth()
    }

    private fun setupCalendarRecyclerView() {
        dayAdapter = DayAdapter { dayItem ->
            selectedDate = dayItem.date
            updateSelectedDay(dayItem)
            loadLogsForDate(dayItem.date)
        }

        binding.recyclerDays.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter
        }
    }

    private fun setupLogsRecyclerView() {
        summaryAdapter = DaySummaryAdapter(
            onFoodEditClick = { logItem ->
                showEditFoodDialog(logItem)
            },
            onFoodDeleteClick = { logItem ->
                showDeleteFoodConfirmation(logItem)
            },
            onExerciseEditClick = { logItem ->
                showEditExerciseDialog(logItem)
            },
            onExerciseDeleteClick = { logItem ->
                showDeleteExerciseConfirmation(logItem)
            }
        )

        binding.recyclerLogs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = summaryAdapter
        }
    }

    private fun setupCurrentMonth() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val today = calendar.time

        // Get all days in current month
        val days = mutableListOf<DayItem>()
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfWeekFormatter = SimpleDateFormat("E", Locale.getDefault())
        val todayFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Map day names to Vietnamese short format
        val dayNameMap = mapOf(
            "Mon" to "T2", "Tue" to "T3", "Wed" to "T4", "Thu" to "T5",
            "Fri" to "T6", "Sat" to "T7", "Sun" to "CN",
            "Thứ 2" to "T2", "Thứ 3" to "T3", "Thứ 4" to "T4", "Thứ 5" to "T5",
            "Thứ 6" to "T6", "Thứ 7" to "T7", "Chủ nhật" to "CN"
        )

        for (day in 1..maxDay) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val date = calendar.time
            val dayOfWeekRaw = dayOfWeekFormatter.format(date)
            val dayOfWeek = dayNameMap[dayOfWeekRaw] ?: dayOfWeekRaw
            val isToday = todayFormatter.format(date) == todayFormatter.format(today)
            
            days.add(DayItem(
                date = date,
                dayOfMonth = day,
                dayOfWeek = dayOfWeek,
                isToday = isToday,
                isSelected = isToday
            ))
        }

        dayAdapter.updateItems(days)
        selectedDate = today

        // Scroll to today
        val todayIndex = days.indexOfFirst { it.isToday }
        if (todayIndex >= 0) {
            binding.recyclerDays.post {
                binding.recyclerDays.scrollToPosition(todayIndex)
            }
        }

        // Load logs for today
        loadLogsForDate(today)
    }

    private fun updateSelectedDay(selectedDay: DayItem) {
        val currentItems = dayAdapter.currentList.toMutableList()
        currentItems.forEachIndexed { index, day ->
            currentItems[index] = day.copy(isSelected = day.date == selectedDay.date)
        }
        dayAdapter.updateItems(currentItems)
    }

    private fun loadLogsForDate(date: Date) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(date)
        
        // Load logs for the selected date
        viewModel.loadFoodHistory(dateStr, dateStr)
        viewModel.loadExerciseHistory(dateStr, dateStr)
    }

    private fun observeViewModel() {
        viewModel.foodHistoryState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    val exerciseState = viewModel.exerciseHistoryState.value
                    if (exerciseState is UiState.Success) {
                        val summary = DaySummary(state.data, exerciseState.data)
                        summaryAdapter.updateSummary(summary)
                        val hasLogs = state.data.isNotEmpty() || exerciseState.data.isNotEmpty()
                        binding.recyclerLogs.visibility = if (hasLogs) View.VISIBLE else View.GONE
                        binding.textEmpty.visibility = if (hasLogs) View.GONE else View.VISIBLE
                    } else if (exerciseState is UiState.Idle || exerciseState is UiState.Loading) {
                        // Wait for exercise state
                    } else {
                        val summary = DaySummary(state.data, emptyList())
                        summaryAdapter.updateSummary(summary)
                        binding.recyclerLogs.visibility = if (state.data.isNotEmpty()) View.VISIBLE else View.GONE
                        binding.textEmpty.visibility = if (state.data.isNotEmpty()) View.GONE else View.VISIBLE
                    }
                    binding.progressBar.visibility = View.GONE
                }
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })

        viewModel.exerciseHistoryState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    val foodState = viewModel.foodHistoryState.value
                    if (foodState is UiState.Success) {
                        val summary = DaySummary(foodState.data, state.data)
                        summaryAdapter.updateSummary(summary)
                        val hasLogs = foodState.data.isNotEmpty() || state.data.isNotEmpty()
                        binding.recyclerLogs.visibility = if (hasLogs) View.VISIBLE else View.GONE
                        binding.textEmpty.visibility = if (hasLogs) View.GONE else View.VISIBLE
                    } else {
                        val summary = DaySummary(emptyList(), state.data)
                        summaryAdapter.updateSummary(summary)
                        binding.recyclerLogs.visibility = if (state.data.isNotEmpty()) View.VISIBLE else View.GONE
                        binding.textEmpty.visibility = if (state.data.isNotEmpty()) View.GONE else View.VISIBLE
                    }
                    binding.progressBar.visibility = View.GONE
                }
                is UiState.Loading -> {
                    // Already handled in foodHistoryState
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })

        // Observe foods and exercises for edit dialogs
        logViewModel.foodsState.observe(viewLifecycleOwner, Observer { state ->
            if (state is UiState.Success) {
                allFoods = state.data
            }
        })

        logViewModel.exercisesState.observe(viewLifecycleOwner, Observer { state ->
            if (state is UiState.Success) {
                allExercises = state.data
            }
        })

        // Observe update/delete states
        viewModel.updateFoodLogState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), "Đã cập nhật log thành công", Toast.LENGTH_SHORT).show()
                    viewModel.resetUpdateStates()
                    selectedDate?.let { loadLogsForDate(it) }
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetUpdateStates()
                }
                else -> {}
            }
        })

        viewModel.deleteFoodLogState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), "Đã xóa log thành công", Toast.LENGTH_SHORT).show()
                    viewModel.resetDeleteStates()
                    selectedDate?.let { loadLogsForDate(it) }
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetDeleteStates()
                }
                else -> {}
            }
        })

        viewModel.updateExerciseLogState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), "Đã cập nhật log thành công", Toast.LENGTH_SHORT).show()
                    viewModel.resetUpdateStates()
                    selectedDate?.let { loadLogsForDate(it) }
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetUpdateStates()
                }
                else -> {}
            }
        })

        viewModel.deleteExerciseLogState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), "Đã xóa log thành công", Toast.LENGTH_SHORT).show()
                    viewModel.resetDeleteStates()
                    selectedDate?.let { loadLogsForDate(it) }
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetDeleteStates()
                }
                else -> {}
            }
        })
    }

    private fun showEditFoodDialog(logItem: com.hienpc.bmiapp.data.model.FoodLogHistoryResponse) {
        // Use the same edit dialog logic from FoodHistoryFragment
        val dialogView = layoutInflater.inflate(com.hienpc.bmiapp.R.layout.dialog_edit_food_log, null)
        val foodSpinner = dialogView.findViewById<android.widget.Spinner>(com.hienpc.bmiapp.R.id.spinnerFood)
        val quantityEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.hienpc.bmiapp.R.id.editTextQuantity)
        val mealTypeSpinner = dialogView.findViewById<android.widget.Spinner>(com.hienpc.bmiapp.R.id.spinnerMealType)

        if (allFoods.isEmpty()) {
            Toast.makeText(requireContext(), "Đang tải danh sách món ăn...", Toast.LENGTH_SHORT).show()
            logViewModel.loadFoods()
            return
        }

        setupFoodSpinner(foodSpinner, allFoods, logItem)

        // Set current values
        val currentQuantityInGrams = if (logItem.unit.lowercase().contains("gram") || logItem.unit.lowercase() == "g") {
            logItem.quantity * 100.0
        } else {
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
        val mealTypeAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mealTypes)
        mealTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mealTypeSpinner.adapter = mealTypeAdapter
        val currentMealType = logItem.mealType ?: "Breakfast"
        val currentMealTypeIndex = mealTypes.indexOfFirst { 
            it.equals(currentMealType, ignoreCase = true) || 
            currentMealType.contains(it, ignoreCase = true)
        }
        if (currentMealTypeIndex >= 0) {
            mealTypeSpinner.setSelection(currentMealTypeIndex)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa log")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val selectedFood = foodSpinner.selectedItem as? com.hienpc.bmiapp.data.model.FoodResponse
                val quantity = quantityEditText.text.toString().toDoubleOrNull()
                val mealType = mealTypeSpinner.selectedItem as? String

                if (selectedFood == null || quantity == null || quantity <= 0 || mealType == null) {
                    Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

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

    private fun showDeleteFoodConfirmation(logItem: com.hienpc.bmiapp.data.model.FoodLogHistoryResponse) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa log này?\n\n${logItem.foodName}\n${logItem.quantity} ${logItem.unit}")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteFoodLog(logItem.logId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditExerciseDialog(logItem: com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse) {
        val dialogView = layoutInflater.inflate(com.hienpc.bmiapp.R.layout.dialog_edit_exercise_log, null)
        val exerciseSpinner = dialogView.findViewById<android.widget.Spinner>(com.hienpc.bmiapp.R.id.spinnerExercise)
        val durationEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.hienpc.bmiapp.R.id.editTextDuration)

        if (allExercises.isEmpty()) {
            Toast.makeText(requireContext(), "Đang tải danh sách bài tập...", Toast.LENGTH_SHORT).show()
            logViewModel.loadExercises()
            return
        }

        setupExerciseSpinner(exerciseSpinner, allExercises, logItem)
        durationEditText.setText(logItem.duration.toInt().toString())

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa log")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val selectedExercise = exerciseSpinner.selectedItem as? com.hienpc.bmiapp.data.model.ExerciseResponse
                val duration = durationEditText.text.toString().toIntOrNull()

                if (selectedExercise == null || duration == null || duration <= 0) {
                    Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.updateExerciseLog(logItem.logId, selectedExercise.id, duration)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showDeleteExerciseConfirmation(logItem: com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa log này?\n\n${logItem.exerciseName}\n${logItem.duration.toInt()} phút")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteExerciseLog(logItem.logId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun setupFoodSpinner(spinner: android.widget.Spinner, foods: List<com.hienpc.bmiapp.data.model.FoodResponse>, currentLog: com.hienpc.bmiapp.data.model.FoodLogHistoryResponse) {
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, foods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        val currentFoodIndex = foods.indexOfFirst { it.id == currentLog.foodId }
        if (currentFoodIndex >= 0) {
            spinner.setSelection(currentFoodIndex)
        }
    }

    private fun setupExerciseSpinner(spinner: android.widget.Spinner, exercises: List<com.hienpc.bmiapp.data.model.ExerciseResponse>, currentLog: com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse) {
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, exercises)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        val currentExerciseIndex = exercises.indexOfFirst { it.id == currentLog.exerciseId }
        if (currentExerciseIndex >= 0) {
            spinner.setSelection(currentExerciseIndex)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

