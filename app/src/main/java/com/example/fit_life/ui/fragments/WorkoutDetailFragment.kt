package com.example.fit_life.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.fit_life.R
import com.example.fit_life.databinding.FragmentWorkoutDetailBinding
import com.example.fit_life.viewmodel.WorkoutViewModel

class WorkoutDetailFragment : Fragment() {

    private var _binding: FragmentWorkoutDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WorkoutViewModel by viewModels()
    private val args: WorkoutDetailFragmentArgs by navArgs()

    // Called to have the fragment instantiate its user interface view.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called immediately after onCreateView has returned, but before any saved state has been restored in to the view.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set labels based on locale
        binding.workoutTypeLabel.text = getString(R.string.workout_type)
        binding.workoutDurationLabel.text = getString(R.string.duration_min)
        binding.workoutCaloriesLabel.text = getString(R.string.calories_burned)
        binding.workoutDateLabel.text = getString(R.string.date_format)
        binding.buttonBack.text = getString(R.string.back)

        // Get workout ID from arguments and observe the workout data
        val workoutId = args.workoutId
        viewModel.getWorkoutLiveData(workoutId).observe(viewLifecycleOwner, Observer { workout ->
            workout?.let {
                binding.workoutType.text = it.type
                binding.workoutDuration.text = it.duration.toString()
                binding.workoutCalories.text = it.caloriesBurned.toString()
                binding.workoutDate.text = it.date
                if (!it.imageUri.isNullOrEmpty()) {
                    binding.workoutImage.setImageURI(Uri.parse(it.imageUri))
                } else {
                    binding.workoutImage.visibility = View.GONE
                }
            }
        })

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    // Called when the view previously created by onCreateView has been detached from the fragment.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
