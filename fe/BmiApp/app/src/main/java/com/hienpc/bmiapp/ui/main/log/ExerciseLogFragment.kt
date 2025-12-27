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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hienpc.bmiapp.R
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
    private var favoriteExercises: List<ExerciseResponse> = emptyList()
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var favoriteAdapter: FavoriteExerciseAdapter
    private var searchJob: Job? = null
    private var selectedExercise: ExerciseResponse? = null
    private var isShowingFavorites: Boolean = false
    private var isRecommendationsExpanded = false
    private var allRecommendations: List<com.hienpc.bmiapp.data.model.RecommendationItem> = emptyList()
    private var isSelectingFromDropdown = false // Flag to prevent resetting selectedExercise when selecting from dropdown

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
        setupFavoritesRecyclerView()
        setupExerciseAutoComplete()
        setupToggleFavoritesButton()
        setupHistoryButton()
        setupListeners()
        observeViewModel()

        viewModel.loadExercises() // Load all exercises initially
        viewModel.loadFavoriteExercises() // Load favorites
        viewModel.loadExerciseRecommendations(10)
    }

    private fun setupRecommendationsRecyclerView() {
        recommendationAdapter = RecommendationAdapter(emptyList()) { recommendedItem ->
            // When user clicks a recommendation, auto-fill the form AND focus on duration
            val exercise = allExercises.find { it.name == recommendedItem.name }
            if (exercise != null) {
                binding.autoCompleteExercise.setText(exercise.name, false)
                selectedExercise = exercise
                // Update calories display
                updateCaloriesDisplay()
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
        
        // Setup expand/collapse button
        binding.buttonShowMoreRecommendations.setOnClickListener {
            isRecommendationsExpanded = !isRecommendationsExpanded
            updateRecommendationsDisplay()
        }
    }
    
    private fun updateRecommendationsDisplay() {
        val allItems = allRecommendations
        if (allItems.size <= 2) {
            // If 2 or fewer items, hide expand button
            binding.buttonShowMoreRecommendations.visibility = View.GONE
            recommendationAdapter.updateItems(allItems)
        } else {
            // Show expand button
            binding.buttonShowMoreRecommendations.visibility = View.VISIBLE
            if (isRecommendationsExpanded) {
                // Show all items
                recommendationAdapter.updateItems(allItems)
                binding.buttonShowMoreRecommendations.text = "Thu gọn"
            } else {
                // Show only first 2 items
                recommendationAdapter.updateItems(allItems.take(2))
                binding.buttonShowMoreRecommendations.text = "Xem thêm ${allItems.size - 2} bài tập"
            }
        }
    }

    private fun setupFavoritesRecyclerView() {
        favoriteAdapter = FavoriteExerciseAdapter(emptyList(),
            onItemClick = { exercise ->
                // When user clicks a favorite, switch to form view and select the exercise
                toggleFavoritesView(false)
                binding.autoCompleteExercise.setText(exercise.name, false)
                selectedExercise = exercise
                // Update calories display
                updateCaloriesDisplay()
                binding.editTextDuration.requestFocus()
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editTextDuration, InputMethodManager.SHOW_IMPLICIT)
            },
            onToggleFavorite = { exercise, isCurrentlyFavorite ->
                viewModel.toggleFavoriteExercise(exercise.id, isCurrentlyFavorite)
            }
        )
        
        binding.recyclerFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
        }
    }

    private fun setupToggleFavoritesButton() {
        binding.buttonToggleFavorites.setOnClickListener {
            isShowingFavorites = !isShowingFavorites
            toggleFavoritesView(isShowingFavorites)
        }
    }

    private fun setupHistoryButton() {
        binding.buttonHistory.setOnClickListener {
            // Navigate to LogHistoryFragment
            val historyFragment = com.hienpc.bmiapp.ui.main.history.LogHistoryFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, historyFragment)
                .addToBackStack("LogHistory")
                .commit()
        }
    }

    private fun toggleFavoritesView(showFavorites: Boolean) {
        isShowingFavorites = showFavorites
        if (showFavorites) {
            binding.cardForm.visibility = View.GONE
            binding.cardFavorites.visibility = View.VISIBLE
            binding.buttonToggleFavorites.text = "Tất cả"
            binding.buttonToggleFavorites.chipIcon = requireContext().getDrawable(android.R.drawable.btn_star_big_on)
            viewModel.loadFavoriteExercises() // Refresh favorites
        } else {
            binding.cardForm.visibility = View.VISIBLE
            binding.cardFavorites.visibility = View.GONE
            binding.buttonToggleFavorites.text = "⭐ Yêu thích"
            binding.buttonToggleFavorites.chipIcon = requireContext().getDrawable(android.R.drawable.btn_star_big_off)
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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Only reset selectedExercise when user types manually (not when selecting from dropdown)
                if (!isSelectingFromDropdown) {
                    android.util.Log.d("ExerciseLog", "User typing, resetting selectedExercise")
                    selectedExercise = null
                    updateCaloriesDisplay() // Update to hide calories info
                } else {
                    android.util.Log.d("ExerciseLog", "Selecting from dropdown, keeping selectedExercise")
                }
            }
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
        binding.autoCompleteExercise.setOnItemClickListener { parent, _, position, _ ->
            android.util.Log.d("ExerciseLog", "=== OnItemClickListener triggered ===")
            android.util.Log.d("ExerciseLog", "Position: $position")
            isSelectingFromDropdown = true // Set flag to prevent TextWatcher from resetting selectedExercise
            val adapter = parent.adapter as? ArrayAdapter<ExerciseResponse>
            val selectedItem = adapter?.getItem(position)
            android.util.Log.d("ExerciseLog", "Selected item: ${selectedItem?.name}")
            if (selectedItem != null) {
                selectedExercise = selectedItem
                android.util.Log.d("ExerciseLog", "selectedExercise set to: ${selectedExercise?.name}")
                android.util.Log.d("ExerciseLog", "Exercise caloriesBurnedPerHour: ${selectedItem.caloriesBurnedPerHour}")
                // Update text to show selected item name (this will trigger TextWatcher, but flag prevents reset)
                binding.autoCompleteExercise.setText(selectedItem.name, false)
                // Reset flag after a short delay to allow TextWatcher to process
                binding.autoCompleteExercise.postDelayed({
                    isSelectingFromDropdown = false
                    android.util.Log.d("ExerciseLog", "Reset isSelectingFromDropdown flag")
                }, 100)
                // Dismiss dropdown
                binding.autoCompleteExercise.dismissDropDown()
                // Update calories display
                android.util.Log.d("ExerciseLog", "Calling updateCaloriesDisplay()...")
                updateCaloriesDisplay()
            } else {
                android.util.Log.e("ExerciseLog", "ERROR: selectedItem is null!")
                isSelectingFromDropdown = false
            }
        }
        
        // Listen to duration changes for realtime calories calculation
        binding.editTextDuration.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateCaloriesDisplay()
            }
        })
    }

    private fun setupListeners() {
        binding.buttonLogExercise.setOnClickListener {
            android.util.Log.d("ExerciseLog", "=== buttonLogExercise clicked ===")
            android.util.Log.d("ExerciseLog", "selectedExercise: ${selectedExercise?.name ?: "null"}")
            val exercise = selectedExercise ?: run {
                // Try to find exercise by name if not selected
                val exerciseName = binding.autoCompleteExercise.text.toString().trim()
                android.util.Log.d("ExerciseLog", "selectedExercise is null, trying to find by name: '$exerciseName'")
                val found = allExercises.find { it.name.equals(exerciseName, ignoreCase = true) }
                android.util.Log.d("ExerciseLog", "Found exercise by name: ${found?.name ?: "null"}")
                found
            }

            if (exercise == null) {
                android.util.Log.e("ExerciseLog", "ERROR: Cannot find exercise!")
                Toast.makeText(requireContext(), "Vui lòng chọn bài tập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            android.util.Log.d("ExerciseLog", "Using exercise: ${exercise.name}, caloriesBurnedPerHour: ${exercise.caloriesBurnedPerHour}")

            val durationText = binding.editTextDuration.text.toString()
            val duration = durationText.toIntOrNull()
            if (duration == null || duration <= 0) {
                Toast.makeText(requireContext(), "Thời gian phải > 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Calculate calories burned for confirmation
            // Backend calculates: (caloriesBurnedPerHour / 60) * durationMinutes
            // So we just send duration in minutes directly
            val caloriesBurned = (exercise.caloriesBurnedPerHour * duration / 60.0).toInt()
            android.util.Log.d("ExerciseLog", "Calculated calories for confirmation: $caloriesBurned kcal")
            android.util.Log.d("ExerciseLog", "Exercise: ${exercise.name}, caloriesBurnedPerHour: ${exercise.caloriesBurnedPerHour}")
            android.util.Log.d("ExerciseLog", "Duration: $duration phút")
            android.util.Log.d("ExerciseLog", "Will send to API: exerciseId=${exercise.id}, durationMinutes=$duration")
            
            // Show confirmation dialog - send duration in minutes directly
            showConfirmationDialog(exercise.name, duration, caloriesBurned, exercise.id, duration)
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
                    android.util.Log.d("ExerciseLog", "Adapter set in observeViewModel, exercises count: ${exercises.size}")
                    
                    // Re-set item click listener (in case it was lost)
                    binding.autoCompleteExercise.setOnItemClickListener { parent, _, position, _ ->
                        android.util.Log.d("ExerciseLog", "=== OnItemClickListener triggered (in observeViewModel) ===")
                        android.util.Log.d("ExerciseLog", "Position: $position")
                        isSelectingFromDropdown = true // Set flag to prevent TextWatcher from resetting selectedExercise
                        val adapter = parent.adapter as? ArrayAdapter<ExerciseResponse>
                        val selectedItem = adapter?.getItem(position)
                        android.util.Log.d("ExerciseLog", "Selected item: ${selectedItem?.name}")
                        if (selectedItem != null) {
                            selectedExercise = selectedItem
                            android.util.Log.d("ExerciseLog", "selectedExercise set to: ${selectedExercise?.name}")
                            // Update text to show selected item name (this will trigger TextWatcher, but flag prevents reset)
                            binding.autoCompleteExercise.setText(selectedItem.name, false)
                            // Reset flag after a short delay to allow TextWatcher to process
                            binding.autoCompleteExercise.postDelayed({
                                isSelectingFromDropdown = false
                                android.util.Log.d("ExerciseLog", "Reset isSelectingFromDropdown flag (in observeViewModel)")
                            }, 100)
                            // Dismiss dropdown
                            binding.autoCompleteExercise.dismissDropDown()
                            // Update calories display
                            android.util.Log.d("ExerciseLog", "Calling updateCaloriesDisplay() from observeViewModel...")
                            updateCaloriesDisplay()
                        } else {
                            android.util.Log.e("ExerciseLog", "ERROR: selectedItem is null in observeViewModel!")
                            isSelectingFromDropdown = false
                        }
                    }
                    
                    // Update calories display if exercise is already selected
                    if (selectedExercise != null) {
                        updateCaloriesDisplay()
                    }

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
                    // Store all recommendations
                    allRecommendations = data.items
                    // Update display (show only 1-2 items initially)
                    updateRecommendationsDisplay()
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

        // Observe favorite exercises
        viewModel.favoriteExercisesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    favoriteExercises = state.data
                    favoriteAdapter.updateItems(favoriteExercises)
                }
                is UiState.Error -> {
                    // Silent fail for favorites
                }
                else -> {}
            }
        }

        // Observe toggle favorite state
        viewModel.toggleFavoriteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    // Favorite toggled successfully, list will auto-refresh
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun updateCaloriesDisplay() {
        android.util.Log.d("ExerciseLog", "=== updateCaloriesDisplay() called ===")
        val exercise = selectedExercise
        val durationText = binding.editTextDuration.text.toString()
        val duration = durationText.toIntOrNull()
        
        android.util.Log.d("ExerciseLog", "selectedExercise: ${exercise?.name ?: "null"}")
        android.util.Log.d("ExerciseLog", "durationText: '$durationText', duration: $duration")
        
        if (exercise != null) {
            if (duration != null && duration > 0) {
                // Calculate calories burned: (caloriesBurnedPerHour / 60) * duration
                val caloriesBurned = (exercise.caloriesBurnedPerHour * duration / 60.0).toInt()
                android.util.Log.d("ExerciseLog", "Calculated calories: $caloriesBurned kcal")
                binding.textCaloriesInfo.text = "Calories đốt cháy: $caloriesBurned kcal"
            } else {
                // Show calories per hour if no duration entered yet
                val displayText = "${exercise.caloriesBurnedPerHour} kcal / giờ"
                android.util.Log.d("ExerciseLog", "No duration, showing per hour: $displayText")
                binding.textCaloriesInfo.text = displayText
            }
            binding.layoutCaloriesInfo.visibility = View.VISIBLE
            android.util.Log.d("ExerciseLog", "Calories info layout set to VISIBLE")
        } else {
            android.util.Log.w("ExerciseLog", "WARNING: exercise is null, hiding calories info")
            binding.layoutCaloriesInfo.visibility = View.GONE
        }
    }

    private fun showConfirmationDialog(
        exerciseName: String,
        duration: Int,
        caloriesBurned: Int,
        exerciseId: Int,
        durationValue: Int // Duration in minutes to send to API
    ) {
        android.util.Log.d("ExerciseLog", "showConfirmationDialog: durationMinutes=$durationValue, caloriesBurned=$caloriesBurned")
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận ghi lại bài tập")
            .setMessage(
                "Bạn đã đốt cháy $caloriesBurned kcal.\n\n" +
                "Bài tập: $exerciseName\n" +
                "Thời gian: $duration phút"
            )
            .setPositiveButton("Xác nhận") { _, _ ->
                android.util.Log.d("ExerciseLog", "Sending to API: exerciseId=$exerciseId, durationMinutes=$durationValue")
                viewModel.logExercise(exerciseId, durationValue)
            }
            .setNegativeButton("Hủy", null)
            .setCancelable(true)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



