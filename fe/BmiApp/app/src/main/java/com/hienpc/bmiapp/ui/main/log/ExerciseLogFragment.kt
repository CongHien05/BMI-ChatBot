package com.hienpc.bmiapp.ui.main.log

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hienpc.bmiapp.data.model.ExerciseResponse
import com.hienpc.bmiapp.databinding.FragmentExerciseLogBinding
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.utils.show
import com.hienpc.bmiapp.utils.hide
import com.hienpc.bmiapp.viewmodel.LogViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExerciseLogFragment : Fragment() {

    private var _binding: FragmentExerciseLogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogViewModel by viewModels()

    private var exercises: List<ExerciseResponse> = emptyList()
    private var allExercises: List<ExerciseResponse> = emptyList() // Store all exercises for filtering
    private lateinit var recommendationAdapter: RecommendationAdapter
    private var searchJob: Job? = null
    private var selectedExercise: ExerciseResponse? = null

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

        setupRecommendationsRecyclerView()
        setupExerciseAutoComplete()
        setupListeners()
        observeViewModel()

        viewModel.loadExercises() // Load all exercises initially
        viewModel.loadExerciseRecommendations(10)
    }
    
    private fun setupRecommendationsRecyclerView() {
        recommendationAdapter = RecommendationAdapter(emptyList()) { recommendedItem ->
            // When user clicks a recommendation, auto-fill the form AND focus on duration
            val exercise = allExercises.find { it.name == recommendedItem.name }
            if (exercise != null) {
                binding.autoCompleteExercise.setText(exercise.name, false)
                selectedExercise = exercise
                // Focus on duration input for better flow
                binding.editTextDuration.requestFocus()
                // Show keyboard
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editTextDuration, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        
        binding.recyclerRecommendations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendationAdapter
        }
    }
    
    private fun setupExerciseAutoComplete() {
        // Create adapter with custom dropdown layout
        val adapter = object : ArrayAdapter<ExerciseResponse>(
            requireContext(),
            R.layout.item_dropdown_exercise
        ) {
            override fun getCount(): Int = exercises.size
            override fun getItem(position: Int): ExerciseResponse = exercises[position]
            override fun getItemId(position: Int): Long = exercises[position].id.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_dropdown_exercise, parent, false)
                
                val textItemName = view.findViewById<android.widget.TextView>(R.id.textItemName)
                textItemName.text = exercises[position].name
                
                return view
            }
        }
        
        binding.autoCompleteExercise.setAdapter(adapter)
        // Disable threshold to show dropdown immediately when there are results
        binding.autoCompleteExercise.threshold = 1
        
        // Handle text change with debounce for search
        binding.autoCompleteExercise.addTextChangedListener(object : TextWatcher {
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
                        // If query is too short, show all exercises
                        viewModel.loadExercises(null)
                    } else {
                        // Search with query
                        viewModel.loadExercises(query)
                    }
                }
            }
        })
        
        // Handle item selection
        binding.autoCompleteExercise.setOnItemClickListener { _, _, position, _ ->
            selectedExercise = exercises[position]
        }
    }

    private fun setupListeners() {
        binding.buttonLogExercise.setOnClickListener {
            val exercise = selectedExercise ?: run {
                // Try to find exercise by name if not selected
                val exerciseName = binding.autoCompleteExercise.text.toString().trim()
                allExercises.find { it.name.equals(exerciseName, ignoreCase = true) }
            }
            
            if (exercise == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn bài tập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val durationText = binding.editTextDuration.text.toString()
            val duration = durationText.toIntOrNull()
            if (duration == null || duration <= 0) {
                Toast.makeText(requireContext(), "Thời gian phải > 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.logExercise(exercise.id, duration)
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
                    // Store all exercises for recommendations and fallback
                    if (allExercises.isEmpty()) {
                        allExercises = exercises
                    }
                    
                    // Create new adapter with custom dropdown layout
                    val adapter = object : ArrayAdapter<ExerciseResponse>(
                        requireContext(),
                        R.layout.item_dropdown_exercise
                    ) {
                        override fun getCount(): Int = exercises.size
                        override fun getItem(position: Int): ExerciseResponse = exercises[position]
                        override fun getItemId(position: Int): Long = exercises[position].id.toLong()
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = convertView ?: LayoutInflater.from(context)
                                .inflate(R.layout.item_dropdown_exercise, parent, false)
                            
                            val textItemName = view.findViewById<android.widget.TextView>(R.id.textItemName)
                            textItemName.text = exercises[position].name
                            
                            return view
                        }
                    }
                    binding.autoCompleteExercise.setAdapter(adapter)
                    
                    // Show dropdown if there are results and text field has focus
                    if (exercises.isNotEmpty() && binding.autoCompleteExercise.hasFocus()) {
                        binding.autoCompleteExercise.showDropDown()
                    }
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
        
        // Observe exercise recommendations
        viewModel.exerciseRecommendationsState.observe(viewLifecycleOwner) { state ->
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


