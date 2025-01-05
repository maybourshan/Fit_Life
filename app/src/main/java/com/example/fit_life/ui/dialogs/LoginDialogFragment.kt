package com.example.fit_life.ui.dialogs

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fit_life.MainActivity
import com.example.fit_life.viewmodel.ProfileViewModel
import com.example.fit_life.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginDialogFragment : DialogFragment() {

    // ViewModel for accessing user profile data
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.dialog_login, container, false)

        // Initialize UI components
        val editTextUserName: EditText = view.findViewById(R.id.editTextUserName)
        val editTextPassword: EditText = view.findViewById(R.id.editTextPassword)
        val buttonLoginUser: Button = view.findViewById(R.id.buttonLoginUser)
        val buttonCancel: Button = view.findViewById(R.id.buttonCancel)

        // Set click listener for login button
        buttonLoginUser.setOnClickListener {
            val userName = editTextUserName.text.toString()
            val password = editTextPassword.text.toString()

            // Validate input fields
            if (userName.isEmpty() || password.isEmpty()) {
                Snackbar.make(view, getString(R.string.please_fill_in_all_fields), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Perform login in a coroutine
            lifecycleScope.launch {
                val userProfile = profileViewModel.getUserProfileByUserName(userName)
                if (userProfile != null && userProfile.password == password) {
                    profileViewModel.setCurrentUser(userName)
                    Snackbar.make(view, getString(R.string.login_successful), Snackbar.LENGTH_LONG).show()

                    // Delay closing the dialog to allow the user to see the message
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }, 800) // 0.8 seconds delay
                } else {
                    Snackbar.make(view, getString(R.string.invalid_username_or_password), Snackbar.LENGTH_LONG).show()
                }
            }
        }

        // Set click listener for cancel button
        buttonCancel.setOnClickListener {
            dismiss() // Close the dialog when cancel button is clicked
        }

        return view
    }
}
