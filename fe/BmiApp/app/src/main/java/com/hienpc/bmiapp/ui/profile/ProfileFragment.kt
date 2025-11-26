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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGenderSpinner()
        setupListeners()
        observeViewModel()
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
                targetWeight = targetWeight,
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


