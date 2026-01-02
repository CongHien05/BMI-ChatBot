package com.hienpc.bmiapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.data.repository.UserRepository
import com.hienpc.bmiapp.data.repository.MeasurementRepository
import com.hienpc.bmiapp.databinding.FragmentDashboardBinding
import com.hienpc.bmiapp.databinding.DialogUpdateMeasurementBinding
import com.hienpc.bmiapp.utils.*
import com.hienpc.bmiapp.viewmodel.DashboardViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hienpc.bmiapp.data.model.*

/**
 * DashboardFragment - hiển thị weight, BMI, calories hôm nay.
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(
            UserRepository(),
            MeasurementRepository()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadDashboard()
            viewModel.loadWeeklySummary()
            viewModel.loadTrendAnalysis()
            viewModel.loadWeightPrediction(7)
            viewModel.loadProfile()
        }

        binding.buttonUpdateMeasurement.setOnClickListener {
            showMeasurementDialog()
        }
        
        // Quick action buttons
        binding.buttonQuickLogFood.setOnClickListener {
            navigateToFoodLog()
        }
        
        binding.buttonQuickLogExercise.setOnClickListener {
            navigateToExerciseLog()
        }

        observeViewModel()
        viewModel.loadDashboard()
        viewModel.loadWeeklySummary()
        viewModel.loadTrendAnalysis()
        viewModel.loadWeightPrediction(7) // Load 7-day prediction
        viewModel.loadProfile() // Load profile for calorie goal
    }
    
    private fun navigateToFoodLog() {
        val foodLogFragment = com.hienpc.bmiapp.ui.main.log.FoodLogFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, foodLogFragment)
            .addToBackStack("Dashboard")
            .commit()
    }
    
    private fun navigateToExerciseLog() {
        val exerciseLogFragment = com.hienpc.bmiapp.ui.main.log.ExerciseLogFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, exerciseLogFragment)
            .addToBackStack("Dashboard")
            .commit()
    }

    private fun observeViewModel() {
        viewModel.dashboardState.observe(viewLifecycleOwner, Observer { state ->
            // Clear all previous states first
            binding.swipeRefresh.isRefreshing = false
            binding.progressBar.visibility = View.GONE
            binding.textError.visibility = View.GONE
            
            // Clear custom states from container
            (binding.root as? ViewGroup)?.clearAllStates()
            
            when (state) {
                is UiState.Idle -> {
                    // Do nothing, just cleared states above
                }
                
                is UiState.Loading -> {
                    if (!binding.swipeRefresh.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
                
                is UiState.Success -> {
                    // Show data
                    val data = state.data
                    val weightText = data.currentWeight?.let {
                        getString(R.string.dashboard_weight_format, it)
                    } ?: getString(R.string.dashboard_weight_placeholder)
                    binding.textCurrentWeight.text = weightText

                    val bmiText = data.bmi?.let {
                        getString(R.string.dashboard_bmi_format, it)
                    } ?: getString(R.string.dashboard_bmi_placeholder)
                    binding.textBmi.text = bmiText
                    
                    // Show BMI status
                    data.bmi?.let { bmi ->
                        val (statusText, statusColor) = getBmiStatus(bmi)
                        binding.textBmiStatus.text = statusText
                        binding.textBmiStatus.setTextColor(statusColor)
                        binding.textBmiStatus.visibility = View.VISIBLE
                    } ?: run {
                        binding.textBmiStatus.visibility = View.GONE
                    }

                    binding.textCalories.text =
                        getString(R.string.dashboard_calories_format, data.totalCaloriesToday)
                    
                    // Update calories progress (will be updated when profile loads too)
                    val profileState = viewModel.profileState.value
                    if (profileState is UiState.Success) {
                        updateCaloriesProgress(data.totalCaloriesToday, profileState.data.dailyCalorieGoal)
                    }
                }
                
                is UiState.Empty -> {
                    // Show empty state
                    (binding.root as? ViewGroup)?.showEmptyState(
                        title = getString(R.string.empty_dashboard),
                        message = state.message,
                        iconRes = R.drawable.ic_dashboard,
                        actionText = "Log bữa ăn",
                        onActionClick = {
                            navigateToFoodLog()
                        }
                    )
                }
                
                is UiState.Error -> {
                    // Show error state with retry
                    (binding.root as? ViewGroup)?.showErrorState(
                        message = state.message,
                        onRetryClick = {
                            viewModel.loadDashboard()
                        }
                    )
                }
            }
        })

        viewModel.measurementState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.buttonUpdateMeasurement.isEnabled = false
                }
                is UiState.Success -> {
                    binding.buttonUpdateMeasurement.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.measurement_update_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetMeasurementState()
                }
                is UiState.Error -> {
                    binding.buttonUpdateMeasurement.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetMeasurementState()
                }
                is UiState.Empty -> {
                    // Should not happen for measurement
                }
                else -> Unit
            }
        })
        
        // Observe weekly summary for charts
        viewModel.weeklySummaryState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val data = state.data
                    // Setup charts
                    ChartHelper.setupWeightChart(binding.chartWeight, data.dailySummaries)
                    ChartHelper.setupCaloriesChart(binding.chartCalories, data.dailySummaries)
                    
                    // Show chart cards
                    binding.cardWeightChart.show()
                    binding.cardCaloriesChart.show()
                }
                is UiState.Error -> {
                    // Hide charts on error
                    binding.cardWeightChart.hide()
                    binding.cardCaloriesChart.hide()
                }
                is UiState.Empty -> {
                    // Hide charts when empty
                    binding.cardWeightChart.hide()
                    binding.cardCaloriesChart.hide()
                }
                else -> {
                    // Loading or Idle - keep current state
                }
            }
        }
        
        // Observe trend analysis for insights
        viewModel.trendAnalysisState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val data = state.data
                    binding.textInsight.text = buildString {
                        append("${getTrendEmoji(data.weightTrend)} ${data.insight}\n\n")
                        
                        if (data.weightChangeRate != 0.0) {
                            val changeText = if (data.weightChangeRate > 0) {
                                "Tăng ${String.format("%.1f", data.weightChangeRate)} kg/tuần"
                            } else {
                                "Giảm ${String.format("%.1f", Math.abs(data.weightChangeRate))} kg/tuần"
                            }
                            append("📊 $changeText\n")
                        }
                        
                        append("🍽️ Trung bình: ${data.avgDailyCalories.toInt()} kcal/ngày\n")
                        append("💪 Trung bình: ${String.format("%.1f", data.avgWeeklyExercises)} lần tập/tuần\n")
                        
                        if (data.daysToGoal != null) {
                            append("\n🎯 Dự kiến đạt mục tiêu sau ${data.daysToGoal} ngày")
                        }
                    }
                    binding.cardTrendInsight.show()
                }
                is UiState.Error -> {
                    binding.cardTrendInsight.hide()
                }
                is UiState.Empty -> {
                    binding.cardTrendInsight.hide()
                }
                else -> {}
            }
        }
        
        // Observe weight prediction (AI)
        viewModel.weightPredictionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val data = state.data
                    
                    // Setup prediction chart
                    ChartHelper.setupWeightPredictionChart(
                        binding.chartWeightPrediction,
                        data.historicalData,
                        data.predictions
                    )
                    
                    // Display AI insights
                    binding.textPredictionInsights.text = data.insights
                    
                    // Show trend indicator
                    val trendText = when (data.metrics.trend) {
                        "INCREASING" -> "📈 Tăng"
                        "DECREASING" -> "📉 Giảm"
                        else -> "➡️ Ổn định"
                    }
                    binding.textPredictionTrend.text = trendText
                    binding.textPredictionTrend.setTextColor(
                        when (data.metrics.trend) {
                            "INCREASING" -> android.graphics.Color.parseColor("#E91E63")
                            "DECREASING" -> android.graphics.Color.parseColor("#4CAF50")
                            else -> android.graphics.Color.parseColor("#9E9E9E")
                        }
                    )
                    
                    // Show card
                    binding.cardWeightPrediction.show()
                }
                is UiState.Error -> {
                    // Hide prediction card on error
                    binding.cardWeightPrediction.hide()
                    Toast.makeText(requireContext(), "Không thể tải dự đoán: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                is UiState.Empty -> {
                    // Hide prediction card when empty
                    binding.cardWeightPrediction.hide()
                }
                is UiState.Loading -> {
                    // Show loading on card
                    binding.textPredictionInsights.text = "⏳ Đang phân tích và dự đoán..."
                }
                else -> {}
            }
        }
        
        // Observe profile for calorie goal
        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val profile = state.data
                    val calorieGoal = profile.dailyCalorieGoal
                    val currentCalories = viewModel.dashboardState.value?.let {
                        if (it is UiState.Success) it.data.totalCaloriesToday else 0
                    } ?: 0
                    
                    updateCaloriesProgress(currentCalories, calorieGoal)
                }
                else -> {}
            }
        }
    }
    
    private fun getBmiStatus(bmi: Double): Pair<String, Int> {
        return when {
            bmi < 18.5 -> Pair("Thiếu cân", android.graphics.Color.parseColor("#2196F3"))
            bmi < 25 -> Pair("Bình thường", android.graphics.Color.parseColor("#4CAF50"))
            bmi < 30 -> Pair("Thừa cân", android.graphics.Color.parseColor("#FF9800"))
            else -> Pair("Béo phì", android.graphics.Color.parseColor("#F44336"))
        }
    }
    
    private fun updateCaloriesProgress(current: Int, goal: Int?) {
        if (goal != null && goal > 0) {
            val progress = ((current.toFloat() / goal.toFloat()) * 100).coerceAtMost(100f).toInt()
            binding.progressCalories.progress = progress
            binding.progressCalories.visibility = View.VISIBLE
            
            val remaining = goal - current
            val goalText = if (remaining > 0) {
                "Còn ${remaining} kcal để đạt mục tiêu"
            } else {
                "Đã vượt mục tiêu ${-remaining} kcal"
            }
            binding.textCaloriesGoal.text = goalText
            binding.textCaloriesGoal.visibility = View.VISIBLE
        } else {
            binding.progressCalories.visibility = View.GONE
            binding.textCaloriesGoal.visibility = View.GONE
        }
    }
    
    private fun getTrendEmoji(trend: String): String {
        return when (trend) {
            "LOSING" -> "📉"
            "GAINING" -> "📈"
            else -> "➡️"
        }
    }

    private fun showMeasurementDialog() {
        val dialogBinding = DialogUpdateMeasurementBinding.inflate(layoutInflater, null, false)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.measurement_dialog_title))
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.measurement_save_button, null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val weightInput = dialogBinding.editTextWeight.text?.toString()?.replace(',', '.')
                val weight = weightInput?.toDoubleOrNull()
                if (weight == null || weight <= 0) {
                    dialogBinding.inputWeight.error = getString(R.string.measurement_weight_error)
                    return@setOnClickListener
                } else {
                    dialogBinding.inputWeight.error = null
                }

                val heightInput = dialogBinding.editTextHeight.text?.toString()?.replace(',', '.')
                val height = heightInput?.toDoubleOrNull()

                viewModel.addMeasurement(weight, height)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


