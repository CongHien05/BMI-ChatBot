package com.hienpc.bmiapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.hienpc.bmiapp.R
import com.hienpc.bmiapp.data.repository.UserRepository
import com.hienpc.bmiapp.databinding.FragmentDashboardBinding
import com.hienpc.bmiapp.utils.UiState
import com.hienpc.bmiapp.viewmodel.DashboardViewModel

/**
 * DashboardFragment - hiển thị weight, BMI, calories hôm nay.
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(UserRepository())
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
        }

        observeViewModel()
        viewModel.loadDashboard()
    }

    private fun observeViewModel() {
        viewModel.dashboardState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UiState.Idle -> {
                    binding.swipeRefresh.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                }
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false

                    val data = state.data
                    binding.textCurrentWeight.text =
                        getString(R.string.dashboard_weight_format, data.currentWeight)
                    binding.textBmi.text =
                        getString(R.string.dashboard_bmi_format, data.bmi)
                    binding.textCalories.text =
                        getString(R.string.dashboard_calories_format, data.totalCaloriesToday)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    binding.textError.visibility = View.VISIBLE
                    binding.textError.text = state.message
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


