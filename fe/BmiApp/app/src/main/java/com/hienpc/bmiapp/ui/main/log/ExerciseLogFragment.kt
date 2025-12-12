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
import com.hienpc.bmiapp.data.model.ExerciseResponse
import com.hienpc.bmiapp.databinding.FragmentExerciseLogBinding
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.viewmodel.LogViewModel

class ExerciseLogFragment : Fragment() {

    private var _binding: FragmentExerciseLogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogViewModel by viewModels()

    private var exercises: List<ExerciseResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()

        viewModel.loadExercises()
    }

    private fun setupListeners() {
        binding.buttonLogExercise.setOnClickListener {
            val selectedPosition = binding.spinnerExercise.selectedItemPosition
            if (selectedPosition == -1 || exercises.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn bài tập", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val selectedExercise = exercises[selectedPosition]

            val durationText = binding.editTextDuration.text.toString()
            val duration = durationText.toIntOrNull()
            if (duration == null || duration <= 0) {
                Toast.makeText(requireContext(), "Thời gian phải > 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.logExercise(selectedExercise.id, duration)
        }
    }

    private fun observeViewModel() {
        viewModel.exercisesState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonLogExercise.isEnabled = false
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonLogExercise.isEnabled = true

                    exercises = state.data
                    val names = exercises.map { it.name }
                    val adapter =
                        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerExercise.adapter = adapter
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonLogExercise.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        })

        viewModel.logExerciseState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Loading -> binding.buttonLogExercise.isEnabled = false
                is UiState.Success -> {
                    binding.buttonLogExercise.isEnabled = true
                    Toast.makeText(requireContext(), "Log bài tập thành công", Toast.LENGTH_SHORT)
                        .show()
                    binding.editTextDuration.text?.clear()
                    viewModel.resetLogStates()
                }
                is UiState.Error -> {
                    binding.buttonLogExercise.isEnabled = true
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


