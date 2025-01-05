package com.example.fit_life.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.fit_life.MainActivity
import com.example.fit_life.data.models.UserProfile
import com.example.fit_life.viewmodel.ProfileViewModel
import com.example.fit_life.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    // ViewModel for accessing user profile data
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var currentUserName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize UI components
        val editTextName: EditText = view.findViewById(R.id.editTextName)
        val editTextAge: EditText = view.findViewById(R.id.editTextAge)
        val editTextWeight: EditText = view.findViewById(R.id.editTextWeight)
        val editTextHeight: EditText = view.findViewById(R.id.editTextHeight)
        val spinnerFitnessGoal: Spinner = view.findViewById(R.id.spinnerFitnessGoal)
        val buttonSave: Button = view.findViewById(R.id.buttonSave)
        val buttonBackToSplash: Button = view.findViewById(R.id.buttonBackToSplash)
        val editUserName: EditText = view.findViewById(R.id.editUserName)
        val editTextPassword: EditText = view.findViewById(R.id.editTextPassword)

        // Observe the current user profile and update the UI accordingly
        profileViewModel.currentUserProfile.observe(viewLifecycleOwner, Observer { userProfile ->
            userProfile?.let {
                currentUserName = it.user_name
                editTextName.setText(it.name)
                editTextAge.setText(it.age.toString())
                editTextWeight.setText(it.weight.toString())
                editTextHeight.setText(it.height.toString())
                editUserName.setText(it.user_name)
                editUserName.isEnabled = false
                editUserName.isFocusable = false
                editTextPassword.setText(it.password)
                val fitnessGoals = resources.getStringArray(R.array.fitness_goals)
                val goalIndex = fitnessGoals.indexOf(it.fitnessGoal)
                if (goalIndex >= 0) {
                    spinnerFitnessGoal.setSelection(goalIndex)
                }
            } ?: run {
                Snackbar.make(view, getString(R.string.user_profile_not_found), Snackbar.LENGTH_LONG).show()
            }
        })

        // Set click listener for save button
        buttonSave.setOnClickListener {
            try {
                val name = editTextName.text.toString()
                val age = editTextAge.text.toString().toInt()
                val weight = editTextWeight.text.toString().toFloat()
                val height = editTextHeight.text.toString().toFloat()
                val fitnessGoal = spinnerFitnessGoal.selectedItem.toString()
                val userName = editUserName.text.toString()
                val password = editTextPassword.text.toString()

                if (name.isEmpty() || userName.isEmpty() || password.isEmpty()) {
                    Snackbar.make(view, getString(R.string.please_fill_in_all_fields), Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val userProfile = UserProfile(
                    name = name,
                    age = age,
                    weight = weight,
                    height = height,
                    fitnessGoal = fitnessGoal,
                    user_name = userName,
                    password = password,
                    current = true
                )

                // Update user profile in the background
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        profileViewModel.updateUser(userProfile)
                    }
                }

                Toast.makeText(requireContext(), getString(R.string.details_saved_successfully), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Snackbar.make(view, getString(R.string.error_saving_profile), Snackbar.LENGTH_LONG).show()
            }
        }

        // Set click listener for back button
        buttonBackToSplash.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }
}
