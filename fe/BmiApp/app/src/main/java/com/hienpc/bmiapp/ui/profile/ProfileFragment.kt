package com.hienpc.bmiapp.ui.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.data.local.AuthPreferences
import com.hienpc.bmiapp.data.local.TokenManager
import com.hienpc.bmiapp.data.model.ProfileUpdateRequest
import com.hienpc.bmiapp.data.repository.UserRepository
import com.hienpc.bmiapp.databinding.FragmentProfileBinding
import com.hienpc.bmiapp.ui.auth.LoginActivity
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModel.Factory(UserRepository())
    }

    private val authPreferences by lazy { AuthPreferences(requireContext().applicationContext) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val achievementAdapter = AchievementAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        android.util.Log.d("ProfileFragment", "onViewCreated called")
        
        setupAchievementsRecyclerView()
        setupGenderSpinner()
        setupListeners()
        observeViewModel()
        loadStreakAndAchievements()
        
        // Initialize stats with 0
        binding.textTotalFoodLogs.text = "0"
        binding.textTotalExerciseLogs.text = "0"
        binding.textDaysActive.text = "0"
    }
    
    private fun setupAchievementsRecyclerView() {
        binding.recyclerAchievements.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = achievementAdapter
        }
    }
    
    private fun loadStreakAndAchievements() {
        viewModel.loadStreak()
        viewModel.loadAchievements()
        viewModel.loadDashboard()
        viewModel.loadMonthlySummary()
        viewModel.loadProfile() // Load profile để lấy target weight
        viewModel.loadTrendAnalysis() // Load trend analysis để lấy days until goal
    }
    
    private fun loadProgressToGoal(currentWeight: Double?, targetWeight: Double?, startWeight: Double?) {
        if (currentWeight == null || targetWeight == null) {
            // Show placeholder if data not available
            binding.textCurrentWeight.text = "Hiện tại: -- kg"
            binding.textTargetWeightDisplay.text = "Mục tiêu: -- kg"
            binding.progressBarWeight.progress = 0
            binding.textProgressPercentage.text = "Chưa có dữ liệu"
            return
        }
        
        val start = startWeight ?: currentWeight // Use current weight as start if no history
        updateProgressBar(currentWeight, targetWeight, start)
    }
    
    private fun updateProgressBar(current: Double, target: Double, start: Double) {
        android.util.Log.d("ProfileFragment", "updateProgressBar called: current=$current, target=$target, start=$start")
        try {
            binding.textCurrentWeight.text = "Hiện tại: %.1f kg".format(current)
            binding.textTargetWeightDisplay.text = "Mục tiêu: %.1f kg".format(target)
            android.util.Log.d("ProfileFragment", "Text views updated")
        
        // Check if goal is achieved
        val difference = current - target
        if (kotlin.math.abs(difference) < 0.1) { // Within 0.1kg tolerance
            binding.progressBarWeight.progress = 100
            binding.textProgressPercentage.text = "Đã đạt mục tiêu! 🎉"
            return
        }
        
        // Determine if goal is to lose or gain weight
        val isLosingWeight = target < start // Target is less than start weight
        val isGainingWeight = target > start // Target is more than start weight
        
        val progressPercentage = if (isLosingWeight) {
            // Goal: Lose weight (target < start)
            // Example: start=70, target=65, current=68
            // Progress = (70-68) / (70-65) * 100 = 2/5 * 100 = 40%
            val totalToLose = start - target
            val lostSoFar = start - current
            if (totalToLose <= 0) 0 else ((lostSoFar / totalToLose) * 100).coerceIn(0.0, 100.0).toInt()
        } else if (isGainingWeight) {
            // Goal: Gain weight (target > start)
            // Example: start=58, target=65, current=60
            // Progress = (60-58) / (65-58) * 100 = 2/7 * 100 = 28.5%
            val totalToGain = target - start
            val gainedSoFar = current - start
            if (totalToGain <= 0) 0 else ((gainedSoFar / totalToGain) * 100).coerceIn(0.0, 100.0).toInt()
        } else {
            // start == target (maintain weight)
            100
        }
        
        binding.progressBarWeight.progress = progressPercentage
        
        // Calculate remaining
        val remaining = if (isLosingWeight) {
            current - target // How much more to lose
        } else {
            target - current // How much more to gain
        }
        
        if (kotlin.math.abs(remaining) < 0.1) {
            binding.textProgressPercentage.text = "Đã đạt mục tiêu! 🎉"
        } else if (isLosingWeight) {
            if (remaining > 0) {
                binding.textProgressPercentage.text = "${progressPercentage}% hoàn thành • Còn giảm ${"%.1f".format(remaining)} kg"
            } else {
                binding.textProgressPercentage.text = "Đã đạt mục tiêu! 🎉"
            }
        } else {
            // Gaining weight
            if (remaining > 0) {
                binding.textProgressPercentage.text = "${progressPercentage}% hoàn thành • Còn tăng ${"%.1f".format(remaining)} kg"
            } else {
                binding.textProgressPercentage.text = "Đã đạt mục tiêu! 🎉"
            }
        }
        android.util.Log.d("ProfileFragment", "updateProgressBar completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Exception in updateProgressBar", e)
            e.printStackTrace()
            throw e // Re-throw to see in caller
        }
    }

    private fun setupGenderSpinner() {
        val items = listOf("Nam", "Nữ", "Khác")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = adapter
    }

    private fun setupListeners() {
        // Update progress bar when target weight changes
        binding.editTextTargetWeight.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { // When user finishes editing
                val targetWeight = binding.editTextTargetWeight.text.toString().toDoubleOrNull()
                if (targetWeight != null) {
                    targetWeightValue = targetWeight
                    updateProgressIfReady()
                }
            }
        }
        
        binding.buttonSaveProfile.setOnClickListener {
            val targetWeight = binding.editTextTargetWeight.text.toString().toDoubleOrNull()
            val calorieGoal = binding.editTextDailyCalorieGoal.text.toString().toIntOrNull()
            val gender = binding.spinnerGender.selectedItem as String

            val request = ProfileUpdateRequest(
                goalWeightKg = targetWeight,
                gender = gender,
                dailyCalorieGoal = calorieGoal
            )

            viewModel.updateProfile(request)
        }

        binding.buttonLogout.setOnClickListener {
            showLogoutDialog()
        }
        
        binding.buttonShareProgress.setOnClickListener {
            shareProgress()
        }
    }

    private fun observeViewModel() {
        viewModel.updateProfileState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.buttonSaveProfile.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    binding.buttonSaveProfile.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Cập nhật profile thành công", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.resetState()
                }
                is UiState.Error -> {
                    binding.buttonSaveProfile.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                else -> Unit
            }
        })
        
        // Observe streak
        viewModel.streakState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val streak = state.data
                    binding.textStreakCurrent.text = getString(R.string.streak_current, streak.currentStreak)
                    binding.textStreakLongest.text = getString(R.string.streak_longest, streak.longestStreak)
                    binding.textStreakMessage.text = streak.message
                }
                is UiState.Error -> {
                    // Show default values
                    binding.textStreakCurrent.text = getString(R.string.streak_current, 0)
                    binding.textStreakLongest.text = getString(R.string.streak_longest, 0)
                    binding.textStreakMessage.text = "Bắt đầu streak của bạn!"
                }
                else -> Unit
            }
        }
        
        // Observe achievements
        viewModel.achievementsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    achievementAdapter.submitList(state.data)
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), "Không thể tải thành tựu", Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
        
        // Observe dashboard for current weight
        viewModel.dashboardState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val currentWeight = state.data.currentWeight
                    android.util.Log.d("ProfileFragment", "Dashboard loaded: currentWeight=$currentWeight")
                    currentWeightValue = currentWeight
                    // Force update progress when dashboard loads
                    updateProgressIfReady()
                }
                is UiState.Error -> {
                    android.util.Log.e("ProfileFragment", "Error loading dashboard: ${state.message}")
                }
                else -> Unit
            }
        }
        
        // Observe profile for target weight and other profile data
        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val profile = state.data
                    android.util.Log.d("ProfileFragment", "Profile loaded: targetWeight=${profile.goalWeightKg}, calorieGoal=${profile.dailyCalorieGoal}")
                    
                    // Pre-fill EditText với target weight từ profile
                    profile.goalWeightKg?.let {
                        binding.editTextTargetWeight.setText(it.toString())
                        targetWeightValue = it // Cache target weight
                    }
                    
                    // Pre-fill daily calorie goal
                    profile.dailyCalorieGoal?.let {
                        binding.editTextDailyCalorieGoal.setText(it.toString())
                    }
                    
                    // Pre-fill gender
                    profile.gender?.let { gender ->
                        val items = listOf("Nam", "Nữ", "Khác")
                        val index = items.indexOf(gender)
                        if (index >= 0) {
                            binding.spinnerGender.setSelection(index)
                        }
                    }
                    
                    // Force update progress when profile loads
                    updateProgressIfReady()
                }
                is UiState.Error -> {
                    android.util.Log.e("ProfileFragment", "Error loading profile: ${state.message}")
                }
                else -> Unit
            }
        }
        
        // Observe monthly summary for start weight (first measurement) and stats
        viewModel.monthlySummaryState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val summary = state.data
                    android.util.Log.d("ProfileFragment", "Monthly summary loaded: ${summary.totalFoodLogs} food, ${summary.totalExerciseLogs} exercise")
                    
                    // Update start weight from first measurement
                    startWeightValue = summary.dailySummaries
                        .firstOrNull { it.weight != null }?.weight
                    
                    android.util.Log.d("ProfileFragment", "Start weight set: $startWeightValue")
                    
                    // Update progress with start weight
                    android.util.Log.d("ProfileFragment", "About to call updateProgressIfReady()")
                    updateProgressIfReady()
                    android.util.Log.d("ProfileFragment", "updateProgressIfReady() completed")
                    
                    // Update profile stats - MUST BE CALLED
                    android.util.Log.d("ProfileFragment", "BEFORE Calling updateProfileStats - summary.totalFoodLogs=${summary.totalFoodLogs}")
                    try {
                        android.util.Log.d("ProfileFragment", "Inside try block, calling updateProfileStats")
                        updateProfileStats(summary)
                        android.util.Log.d("ProfileFragment", "AFTER updateProfileStats completed successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileFragment", "EXCEPTION in updateProfileStats", e)
                        e.printStackTrace()
                    }
                    android.util.Log.d("ProfileFragment", "After try-catch block")
                }
                is UiState.Error -> {
                    android.util.Log.e("ProfileFragment", "Error loading monthly summary: ${state.message}")
                }
                else -> Unit
            }
        }
        
        // Observe trend analysis for days until goal
        viewModel.trendAnalysisState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val daysToGoal = state.data.daysToGoal
                    updateDaysUntilGoal(daysToGoal)
                }
                is UiState.Error -> {
                    android.util.Log.e("ProfileFragment", "Error loading trend analysis: ${state.message}")
                    binding.textDaysUntilGoal.text = ""
                }
                else -> Unit
            }
        }
    }
    
    private fun updateDaysUntilGoal(daysToGoal: Int?) {
        if (daysToGoal == null || daysToGoal <= 0) {
            binding.textDaysUntilGoal.text = ""
            return
        }
        
        when {
            daysToGoal == 1 -> {
                binding.textDaysUntilGoal.text = "Dự kiến đạt mục tiêu trong 1 ngày nữa! 🎯"
            }
            daysToGoal < 7 -> {
                binding.textDaysUntilGoal.text = "Dự kiến đạt mục tiêu trong $daysToGoal ngày nữa! 🎯"
            }
            daysToGoal < 30 -> {
                val weeks = (daysToGoal / 7.0).toInt()
                binding.textDaysUntilGoal.text = "Dự kiến đạt mục tiêu trong khoảng $weeks tuần nữa 📅"
            }
            else -> {
                val months = (daysToGoal / 30.0).toInt()
                binding.textDaysUntilGoal.text = "Dự kiến đạt mục tiêu trong khoảng $months tháng nữa 📅"
            }
        }
    }
    
    private var currentWeightValue: Double? = null
    private var startWeightValue: Double? = null
    private var targetWeightValue: Double? = null
    
    /**
     * Update progress bar if both current weight and target weight are available
     */
    private fun updateProgressIfReady() {
        // Get current weight
        val current = currentWeightValue 
            ?: viewModel.dashboardState.value?.let {
                if (it is UiState.Success) it.data.currentWeight else null
            }
        
        // Get target weight from multiple sources
        val target = targetWeightValue 
            ?: binding.editTextTargetWeight.text.toString().toDoubleOrNull()
            ?: viewModel.profileState.value?.let {
                if (it is UiState.Success) it.data.goalWeightKg else null
            }
        
        // Update cached values
        if (current != null) currentWeightValue = current
        if (target != null) targetWeightValue = target
        
        // Update progress bar if we have both current and target weight
        if (current != null && target != null) {
            android.util.Log.d("ProfileFragment", "Updating progress: current=$current, target=$target, start=$startWeightValue")
            try {
                loadProgressToGoal(current, target, startWeightValue)
                android.util.Log.d("ProfileFragment", "loadProgressToGoal completed")
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "Exception in loadProgressToGoal", e)
                e.printStackTrace()
            }
        } else {
            // Show placeholder if data not available
            android.util.Log.d("ProfileFragment", "Progress not ready: current=$current, target=$target")
            binding.textCurrentWeight.text = "Hiện tại: -- kg"
            binding.textTargetWeightDisplay.text = "Mục tiêu: -- kg"
            binding.progressBarWeight.progress = 0
            if (target == null) {
                binding.textProgressPercentage.text = "Chưa có dữ liệu. Vui lòng nhập mục tiêu cân nặng."
            } else {
                binding.textProgressPercentage.text = "Đang tải dữ liệu..."
            }
        }
    }
    
    private fun updateProgressWithData(currentWeight: Double?, targetWeight: Double?, startWeight: Double? = null) {
        // Update cached values
        currentWeightValue = currentWeight ?: currentWeightValue
        startWeightValue = startWeight ?: startWeightValue
        targetWeightValue = targetWeight ?: targetWeightValue
        
        // Update progress bar
        updateProgressIfReady()
    }
    
    private fun updateProfileStats(summary: com.hienpc.bmiapp.data.model.MonthlySummaryResponse) {
        try {
            // Total food logs
            val totalFoodLogs = summary.totalFoodLogs
            binding.textTotalFoodLogs.text = totalFoodLogs.toString()
            
            // Total exercise logs (workouts)
            val totalExerciseLogs = summary.totalExerciseLogs
            binding.textTotalExerciseLogs.text = totalExerciseLogs.toString()
            
            // Days active: số ngày có ít nhất 1 log (food hoặc exercise)
            val daysActive = summary.dailySummaries.count { 
                it.foodLogsCount > 0 || it.exerciseLogsCount > 0 
            }
            binding.textDaysActive.text = daysActive.toString()
            
            // Debug logging
            android.util.Log.d("ProfileFragment", "Stats updated: Food=$totalFoodLogs, Exercise=$totalExerciseLogs, Days=$daysActive")
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error updating stats", e)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout_dialog_title))
            .setMessage(getString(R.string.logout_dialog_message))
            .setPositiveButton(getString(R.string.logout_button_confirm)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.logout_button_cancel), null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            // Clear token từ DataStore
            authPreferences.clearToken()
            
            // Clear token từ TokenManager
            TokenManager.token = null
            
            // Show toast
            Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
            
            // Navigate về LoginActivity và clear back stack
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            
            // Finish MainActivity
            requireActivity().finish()
        }
    }
    
    private fun shareProgress() {
        try {
            // Capture Progress to Goal card
            val cardView = binding.cardProgressToGoal
            val bitmap = captureView(cardView)
            
            if (bitmap != null) {
                // Save bitmap to file
                val imageFile = saveBitmapToFile(bitmap)
                
                if (imageFile != null) {
                    // Create share intent
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, getUriForFile(imageFile))
                        putExtra(Intent.EXTRA_TEXT, buildShareText())
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    // Start share activity
                    startActivity(Intent.createChooser(shareIntent, "Chia sẻ tiến độ"))
                } else {
                    Toast.makeText(requireContext(), "Không thể lưu ảnh", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Không thể chụp ảnh", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error sharing progress", e)
            Toast.makeText(requireContext(), "Lỗi khi chia sẻ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun captureView(view: View): Bitmap? {
        return try {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            
            val bitmap = Bitmap.createBitmap(
                view.measuredWidth,
                view.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error capturing view", e)
            null
        }
    }
    
    private fun saveBitmapToFile(bitmap: Bitmap): File? {
        return try {
            val cacheDir = requireContext().cacheDir
            val imageFile = File(cacheDir, "progress_share_${System.currentTimeMillis()}.png")
            
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            imageFile
        } catch (e: IOException) {
            android.util.Log.e("ProfileFragment", "Error saving bitmap", e)
            null
        }
    }
    
    private fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }
    
    private fun buildShareText(): String {
        val currentWeight = currentWeightValue
        val targetWeight = targetWeightValue
        val progressText = binding.textProgressPercentage.text.toString()
        val daysText = binding.textDaysUntilGoal.text.toString()
        
        val shareText = StringBuilder()
        shareText.append("🎯 Tiến độ mục tiêu của tôi:\n\n")
        
        if (currentWeight != null && targetWeight != null) {
            shareText.append("Hiện tại: ${"%.1f".format(currentWeight)} kg\n")
            shareText.append("Mục tiêu: ${"%.1f".format(targetWeight)} kg\n")
        }
        
        shareText.append("$progressText\n")
        
        if (daysText.isNotEmpty()) {
            shareText.append("$daysText\n")
        }
        
        shareText.append("\n#BMIApp #FitnessJourney #HealthGoals")
        
        return shareText.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


