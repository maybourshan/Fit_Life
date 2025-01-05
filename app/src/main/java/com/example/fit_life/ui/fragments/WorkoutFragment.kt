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
import com.example.fit_life.MainActivity
import com.example.fit_life.R
import com.example.fit_life.utils.UserSessionManager
import com.example.fit_life.data.models.Workout
import com.example.fit_life.adapters.WorkoutAdapter
import com.example.fit_life.viewmodel.WorkoutViewModel
import com.example.fit_life.databinding.FragmentWorkoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutFragment : Fragment() {

    // Binding for the fragment layout
    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WorkoutViewModel by viewModels()
    private lateinit var adapter: WorkoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the WorkoutAdapter
        adapter = WorkoutAdapter(
            context = requireContext(),
            onItemClick = { workout ->
                val action = WorkoutFragmentDirections.actionWorkoutFragmentToWorkoutDetailFragment(workout.id)
                findNavController().navigate(action)
            },
            onEditClick = { workout ->
                val action = WorkoutFragmentDirections.actionWorkoutFragmentToAddEditWorkoutFragment(workout.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { showDeleteConfirmationDialog(it) }
        )
        binding.recyclerViewWorkouts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewWorkouts.adapter = adapter

        // Observe workout list and update the adapter
        viewModel.workoutList.observe(viewLifecycleOwner, Observer { workouts ->
            adapter.submitList(workouts.toMutableList())
        })

        // Set click listener for adding a new workout
        binding.addWorkoutButton.setOnClickListener {
            val action = WorkoutFragmentDirections.actionWorkoutFragmentToAddEditWorkoutFragment(null)
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

    // Observe the current user profile and load workouts
    private fun observeCurrentUserProfile() {
        UserSessionManager.getCurrentUserProfile(requireContext()).observe(viewLifecycleOwner, Observer { userProfile ->
            userProfile?.let { profile ->
                loadWorkoutsFromDatabase(profile.user_name)
            }
        })
    }

    // Show a confirmation dialog before deleting a workout
    private fun showDeleteConfirmationDialog(workout: Workout) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_workout))
            .setMessage(getString(R.string.confirm_delete_workout))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        viewModel.deleteWorkout(workout)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Load workouts from the database for the specified user
    private fun loadWorkoutsFromDatabase(userName: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                viewModel.loadWorkouts(userName)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
