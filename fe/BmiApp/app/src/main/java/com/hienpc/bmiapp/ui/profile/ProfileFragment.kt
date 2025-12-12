package com.hienpc.bmiapp.ui.profile

import android.content.Intent
import android.os.Bundle
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

        setupAchievementsRecyclerView()
        setupGenderSpinner()
        setupListeners()
        observeViewModel()
        loadStreakAndAchievements()
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
        loadProgressToGoal()
    }
    
    private fun loadProgressToGoal() {
        // TODO: Load from dashboard API or profile
        // For now, using placeholder values
        val currentWeight = 68.5 // From latest measurement
        val targetWeight = 65.0 // From profile
        val startWeight = 70.0 // From first measurement
        
        updateProgressBar(currentWeight, targetWeight, startWeight)
    }
    
    private fun updateProgressBar(current: Double, target: Double, start: Double) {
        binding.textCurrentWeight.text = "Hiện tại: %.1f kg".format(current)
        binding.textTargetWeightDisplay.text = "Mục tiêu: %.1f kg".format(target)
        
        if (start == target) {
            binding.progressBarWeight.progress = 100
            binding.textProgressPercentage.text = "Đã đạt mục tiêu! 🎉"
            return
        }
        
        val totalToLose = start - target
        val lostSoFar = start - current
        val progressPercentage = ((lostSoFar / totalToLose) * 100).coerceIn(0.0, 100.0).toInt()
        
        binding.progressBarWeight.progress = progressPercentage
        binding.textProgressPercentage.text = "$progressPercentage% hoàn thành"
        
        val remaining = current - target
        if (remaining > 0) {
            binding.textProgressPercentage.append(" • Còn %.1f kg".format(remaining))
        } else {
            binding.textProgressPercentage.text = "Đã đạt mục tiêu! 🎉"
        }
    }

    private fun setupGenderSpinner() {
        val items = listOf("Nam", "Nữ", "Khác")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = adapter
    }

    private fun setupListeners() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


