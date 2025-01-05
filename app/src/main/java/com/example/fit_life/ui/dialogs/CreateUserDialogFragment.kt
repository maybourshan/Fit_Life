package com.example.fit_life.ui.dialogs

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fit_life.data.models.UserProfile
import com.example.fit_life.viewmodel.ProfileViewModel
import com.example.fit_life.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

// DialogFragment for creating a new user profile
class CreateUserDialogFragment : DialogFragment() {

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_user_dialog, container, false)

        // Initialize UI elements
        val editTextName: EditText = view.findViewById(R.id.editTextName)
        val editTextAge: EditText = view.findViewById(R.id.editTextAge)
        val editTextWeight: EditText = view.findViewById(R.id.editTextWeight)
        val editTextHeight: EditText = view.findViewById(R.id.editTextHeight)
        val spinnerFitnessGoal: Spinner = view.findViewById(R.id.spinnerFitnessGoal)
        val editUserName: EditText = view.findViewById(R.id.editUserName)
        val editTextPassword: EditText = view.findViewById(R.id.editTextPassword)
        val buttonSaveUser: Button = view.findViewById(R.id.buttonSaveUser)
        val buttonCancel: Button = view.findViewById(R.id.buttonCancel)

        // Set click listener for save button
        buttonSaveUser.setOnClickListener {
            val name = editTextName.text.toString()
            val age = editTextAge.text.toString().toIntOrNull()
            val weight = editTextWeight.text.toString().toFloatOrNull()
            val height = editTextHeight.text.toString().toFloatOrNull()
            val fitnessGoal = spinnerFitnessGoal.selectedItem.toString()
            val userName = editUserName.text.toString()
            val password = editTextPassword.text.toString()

            // Validate input fields
            if (name.isEmpty() || age == null || weight == null || height == null || userName.isEmpty() || password.isEmpty()) {
                Snackbar.make(view, getString(R.string.please_fill_in_all_fields), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Check if the username already exists
                val existingUser = profileViewModel.getUserProfileByUserName(userName)
                if (existingUser != null) {
                    Snackbar.make(view, getString(R.string.username_already_exists), Snackbar.LENGTH_LONG).show()
                    return@launch
                }

                // Create new user profile and insert it into the database
                val userProfile = UserProfile(
                    name = name,
                    age = age,
                    weight = weight,
                    height = height,
                    fitnessGoal = fitnessGoal,
                    user_name = userName,
                    password = password,
                    current = false
                )
                profileViewModel.insert(userProfile)

                // Show a success message
                Snackbar.make(view, getString(R.string.user_created_successfully), Snackbar.LENGTH_LONG).show()

                // Delay closing the dialog to allow the user to see the message
                Handler(Looper.getMainLooper()).postDelayed({
                    dismiss() // Close the dialog after saving the user
                }, 1500) // 1.5 seconds delay
            }
        }

        // Set click listener for cancel button
        buttonCancel.setOnClickListener {
            dismiss() // Close the dialog when cancel button is clicked
        }

        return view
    }
}
