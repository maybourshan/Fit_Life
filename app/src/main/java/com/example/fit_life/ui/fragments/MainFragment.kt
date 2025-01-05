package com.example.fit_life.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fit_life.ui.dialogs.CreateUserDialogFragment
import com.example.fit_life.ui.dialogs.LoginDialogFragment
import com.example.fit_life.viewmodel.ProfileViewModel
import com.example.fit_life.R
import com.example.fit_life.databinding.FragmentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {

    // View binding for accessing UI components
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    // ViewModel for accessing user profile data
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the current user's profile and display a welcome message if available
        lifecycleScope.launch {
            val userProfile = withContext(Dispatchers.IO) { profileViewModel.getCurrentUserProfile() }
            userProfile?.let {
                val welcomeMessage = getString(R.string.welcome_back, it.name)
                binding.welcomeTextView.text = welcomeMessage
                binding.welcomeTextView.visibility = View.VISIBLE
            }
        }

        // Set click listener for create user button
        binding.buttonCreateUser.setOnClickListener {
            showCreateUserDialog()
        }

        // Set click listener for login button
        binding.buttonLogin.setOnClickListener {
            showLoginDialog()
        }

        // Set click listeners for navigation buttons
        binding.navigateProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_profileFragment)
        }

        binding.navigateWorkoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_workoutFragment)
        }

        binding.navigateDietButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_dietFragment)
        }

        binding.navigateProgressButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_progressFragment)
        }

        binding.showGymsButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_gymListFragment)
        }
    }

    // Show the create user dialog
    private fun showCreateUserDialog() {
        val dialog = CreateUserDialogFragment()
        dialog.show(parentFragmentManager, "CreateUserDialogFragment")
    }

    // Show the login dialog
    private fun showLoginDialog() {
        val dialog = LoginDialogFragment()
        dialog.show(parentFragmentManager, "LoginDialogFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding when the view is destroyed
        _binding = null
    }
}
