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
import com.example.fit_life.databinding.FragmentMealDetailBinding
import com.example.fit_life.viewmodel.MealViewModel

class MealDetailFragment : Fragment() {

    private var _binding: FragmentMealDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MealViewModel by viewModels()
    private val args: MealDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set labels based on locale
        binding.mealNameLabel.text = getString(R.string.meal_name_label)
        binding.mealCaloriesLabel.text = getString(R.string.meal_calories_label)
        binding.mealDateLabel.text = getString(R.string.meal_date_label)
        binding.buttonBack.text = getString(R.string.back)

        // Get meal ID from arguments and observe the meal data
        val mealId = args.mealId
        viewModel.getMealLiveData(mealId).observe(viewLifecycleOwner, Observer { meal ->
            meal?.let {
                binding.mealName.text = it.name
                binding.mealCalories.text = it.calories.toString()
                binding.mealDate.text = it.date
                if (!it.imageUri.isNullOrEmpty()) {
                    binding.mealImage.setImageURI(Uri.parse(it.imageUri))
                } else {
                    binding.mealImage.visibility = View.GONE
                }
            }
        })

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
