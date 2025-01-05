package com.example.fit_life.ui.fragments

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fit_life.MainActivity
import com.example.fit_life.R
import com.example.fit_life.databinding.FragmentGymListBinding
import androidx.navigation.fragment.findNavController
import com.example.fit_life.adapters.GymAdapter
import com.example.fit_life.viewmodel.GymListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class GymListFragment : Fragment() {

    // Variable to hold the binding for the view
    private var _binding: FragmentGymListBinding? = null
    private val binding get() = _binding!!
    private lateinit var gymAdapter: GymAdapter
    private val gymListViewModel: GymListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGymListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the title with different font sizes
        val titleText = getString(R.string.gym_list_title)
        val spannableTitle = SpannableStringBuilder(titleText)
        val mainTitleSize = 24
        val subTitleSize = 18
        val mainTitleEndIndex = if (isHebrewLocale()) 41 else 56  // Adjusted for Hebrew length
        spannableTitle.setSpan(AbsoluteSizeSpan(mainTitleSize, true), 0, mainTitleEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableTitle.setSpan(AbsoluteSizeSpan(subTitleSize, true), mainTitleEndIndex, spannableTitle.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.gymListTitle.text = spannableTitle

        // Initialize the RecyclerView adapter
        gymAdapter = GymAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = gymAdapter

        // Observe changes in the gym list from the ViewModel
        gymListViewModel.gyms.observe(viewLifecycleOwner) { gyms ->
            gyms?.let {
                gymAdapter.updateGyms(gyms)
            }
        }

        // Get the gym list from the MainActivity and set it in the ViewModel
        (activity as? MainActivity)?.let {
            lifecycleScope.launch {
                val gyms = withContext(Dispatchers.IO) { it.getGymList().value }
                gyms?.let { gymListViewModel.setGyms(gyms) }
            }
        }

        // Set click listener for the back button
        binding.buttonBackToMain.setOnClickListener {
            findNavController().navigate(R.id.action_gymListFragment_to_mainFragment)
        }

        // Set click listener for the show map button
        binding.buttonShowMap.setOnClickListener {
            val gymsArray = gymListViewModel.gyms.value?.toTypedArray() ?: arrayOf()
            val bundle = Bundle().apply {
                putParcelableArray("gyms", gymsArray)
            }
            findNavController().navigate(R.id.action_gymListFragment_to_gymMapFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding object
        _binding = null
    }

    // Function to check if the current locale is Hebrew
    private fun isHebrewLocale(): Boolean {
        return Locale.getDefault().language == "iw" || Locale.getDefault().language == "he"
    }
}
