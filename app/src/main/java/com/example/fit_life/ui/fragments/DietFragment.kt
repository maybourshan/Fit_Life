package com.example.fit_life.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fit_life.data.models.Meal
import com.example.fit_life.adapters.MealAdapter
import com.example.fit_life.viewmodel.MealViewModel
import com.example.fit_life.MainActivity
import com.example.fit_life.R
import com.example.fit_life.utils.UserSessionManager
import com.example.fit_life.databinding.FragmentDietBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DietFragment : Fragment() {

    // Binding for the fragment layout
    private var _binding: FragmentDietBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MealViewModel by viewModels()

    private lateinit var adapter: MealAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDietBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the MealAdapter
        adapter = MealAdapter(
            context = requireContext(),
            onItemClick = { meal ->
                val action = DietFragmentDirections.actionDietFragmentToMealDetailFragment(meal.id)
                findNavController().navigate(action)
            },
            onEditClick = { meal ->
                val action = DietFragmentDirections.actionDietFragmentToAddEditMealFragment(meal.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { showDeleteConfirmationDialog(it) }
        )
        binding.recyclerViewMeals.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMeals.adapter = adapter

        // Observe meal list and update the adapter
        viewModel.mealList.observe(viewLifecycleOwner, Observer { meals ->
            adapter.submitList(meals.toMutableList())
        })

        // Set click listener for adding a new meal
        binding.addMealButton.setOnClickListener {
            val action = DietFragmentDirections.actionDietFragmentToAddEditMealFragment(null)
            findNavController().navigate(action)
        }

        // Set click listener for returning to splash screen
        binding.buttonBackToSplash.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // Observe current user profile
        observeCurrentUserProfile()
    }

    // Observe the current user profile and load meals
    private fun observeCurrentUserProfile() {
        UserSessionManager.getCurrentUserProfile(requireContext()).observe(viewLifecycleOwner, Observer { userProfile ->
            userProfile?.let { profile ->
                loadMealsFromDatabase(profile.user_name)
            }
        })
    }

    // Show a confirmation dialog before deleting a meal
    private fun showDeleteConfirmationDialog(meal: Meal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_meal))
            .setMessage(getString(R.string.confirm_delete_meal))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        viewModel.deleteMeal(meal)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Load meals from the database for the specified user
    private fun loadMealsFromDatabase(userName: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                viewModel.loadMeals(userName)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
