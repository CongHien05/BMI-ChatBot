package com.hienpc.bmiapp.ui.main.history

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.data.model.ExerciseLogHistoryResponse
import com.hienpc.bmiapp.data.model.ExerciseResponse
import com.hienpc.bmiapp.databinding.FragmentExerciseHistoryBinding
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.viewmodel.LogHistoryViewModel
import com.hienpc.bmiapp.viewmodel.LogViewModel

/**
 * ExerciseHistoryFragment - hiển thị lịch sử các bài tập đã log
 */
class ExerciseHistoryFragment : Fragment() {

    private var _binding: FragmentExerciseHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogHistoryViewModel by viewModels()
    private val logViewModel: LogViewModel by viewModels() // To load exercises list
    private lateinit var adapter: ExerciseHistoryAdapter
    private var allExercises: List<ExerciseResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        
        // Load exercises list for edit dialog
        logViewModel.loadExercises()
        
        // Load last 30 days by default
        viewModel.loadExerciseHistory()
    }

    private fun setupRecyclerView() {
        adapter = ExerciseHistoryAdapter(
            onEditClick = { logItem ->
                showEditDialog(logItem)
            },
            onDeleteClick = { logItem ->
                showDeleteConfirmation(logItem)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ExerciseHistoryFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.exerciseHistoryState.observe(viewLifecycleOwner, Observer { state ->
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

        viewModel.deleteExerciseLogState.observe(viewLifecycleOwner, Observer { state ->
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

        viewModel.updateExerciseLogState.observe(viewLifecycleOwner, Observer { state ->
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

        // Observe exercises list for edit dialog
        logViewModel.exercisesState.observe(viewLifecycleOwner, Observer { state ->
            if (state is UiState.Success) {
                allExercises = state.data
            }
        })
    }

    private fun showDeleteConfirmation(logItem: ExerciseLogHistoryResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa log này?\n\n${logItem.exerciseName}\n${logItem.duration.toInt()} phút")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteExerciseLog(logItem.logId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditDialog(logItem: ExerciseLogHistoryResponse) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_exercise_log, null)
        val exerciseSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerExercise)
        val durationEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextDuration)

        // Load exercises into spinner
        if (allExercises.isEmpty()) {
            Toast.makeText(requireContext(), "Đang tải danh sách bài tập...", Toast.LENGTH_SHORT).show()
            logViewModel.loadExercises()
            // Wait for exercises to load
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (allExercises.isNotEmpty()) {
                    setupExerciseSpinner(exerciseSpinner, allExercises, logItem)
                }
            }, 500)
        } else {
            setupExerciseSpinner(exerciseSpinner, allExercises, logItem)
        }

        // Set current duration
        durationEditText.setText(logItem.duration.toInt().toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa log")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val selectedExercise = exerciseSpinner.selectedItem as? ExerciseResponse
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

    private fun setupExerciseSpinner(spinner: android.widget.Spinner, exercises: List<ExerciseResponse>, currentLog: ExerciseLogHistoryResponse) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, exercises)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        // Select current exercise
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

