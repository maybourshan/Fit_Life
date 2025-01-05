package com.example.fit_life.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.fit_life.R
import com.example.fit_life.utils.UserSessionManager
import com.example.fit_life.data.models.Workout
import com.example.fit_life.viewmodel.WorkoutViewModel
import com.example.fit_life.databinding.FragmentAddEditWorkoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.Date

class AddEditWorkoutFragment : Fragment() {

    // Binding for the fragment layout
    private var _binding: FragmentAddEditWorkoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WorkoutViewModel by viewModels()
    private val args: AddEditWorkoutFragmentArgs by navArgs()

    private var imageUri: Uri? = null

    // Launcher for requesting gallery permission
    private val requestGalleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), getString(R.string.gallery_access_denied), Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher for requesting camera permission
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), getString(R.string.camera_access_denied), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up button click listeners
        binding.buttonAddImage.setOnClickListener {
            val options = arrayOf(getString(R.string.camera), getString(R.string.gallery))
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.select_image))
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> checkCameraPermission()
                        1 -> checkGalleryPermission()
                    }
                }
                .show()
        }

        binding.buttonRemoveImage.setOnClickListener {
            imageUri = null
            binding.imageViewWorkout.setImageURI(null)
            binding.imageContainer.visibility = View.GONE
        }

        val workoutId = args.workoutId
        if (workoutId != null) {
            // Load workout data if editing an existing workout
            lifecycleScope.launch {
                val workout = withContext(Dispatchers.IO) { viewModel.getWorkoutById(workoutId) }
                workout?.let {
                    populateFields(it)
                    binding.buttonSaveWorkout.text = getString(R.string.update)
                }
            }
        } else {
            // Set up for adding a new workout
            binding.buttonSaveWorkout.text = getString(R.string.add)
            val currentDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date())
            binding.editTextWorkoutDate.setText(currentDate)
        }

        // Set click listener for saving the workout
        binding.buttonSaveWorkout.setOnClickListener {
            if (workoutId == null) {
                addWorkout()
            } else {
                updateWorkout(workoutId)
            }
        }

        // Set click listener for canceling the operation
        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    // Populate fields with workout data
    private fun populateFields(workout: Workout) {
        binding.editTextWorkoutType.setText(workout.type)
        binding.editTextWorkoutDuration.setText(workout.duration.toString())
        binding.editTextWorkoutCaloriesBurned.setText(workout.caloriesBurned.toString())
        binding.editTextWorkoutDate.setText(workout.date)
        workout.imageUri?.let {
            imageUri = Uri.parse(it)
            binding.imageContainer.visibility = View.VISIBLE
            binding.imageViewWorkout.setImageURI(imageUri)
        }
    }

    // Add a new workout
    private fun addWorkout() {
        val type = binding.editTextWorkoutType.text.toString().trim()
        val duration = binding.editTextWorkoutDuration.text.toString().toIntOrNull() ?: 0
        val caloriesBurned = binding.editTextWorkoutCaloriesBurned.text.toString().toIntOrNull() ?: 0
        val date = binding.editTextWorkoutDate.text.toString().trim()

        if (type.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.please_fill_out_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        // Observe the current user profile
        UserSessionManager.getCurrentUserProfile(requireContext()).observe(viewLifecycleOwner, Observer { userProfile ->
            userProfile?.let { profile ->
                val workout = Workout(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    duration = duration,
                    caloriesBurned = caloriesBurned,
                    date = date,
                    imageUri = imageUri?.toString(),
                    userName = profile.user_name
                )

                // Add workout to database
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        viewModel.addWorkout(workout)
                    }
                    findNavController().navigateUp()
                }
            } ?: run {
                Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Update an existing workout
    private fun updateWorkout(workoutId: String) {
        val type = binding.editTextWorkoutType.text.toString().trim()
        val duration = binding.editTextWorkoutDuration.text.toString().toIntOrNull() ?: 0
        val caloriesBurned = binding.editTextWorkoutCaloriesBurned.text.toString().toIntOrNull() ?: 0
        val date = binding.editTextWorkoutDate.text.toString().trim()

        if (type.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.please_fill_out_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        // Observe the current user profile
        UserSessionManager.getCurrentUserProfile(requireContext()).observe(viewLifecycleOwner, Observer { userProfile ->
            userProfile?.let { profile ->
                lifecycleScope.launch {
                    val workout = withContext(Dispatchers.IO) { viewModel.getWorkoutById(workoutId) }
                    workout?.let {
                        val updatedWorkout = it.copy(
                            type = type,
                            duration = duration,
                            caloriesBurned = caloriesBurned,
                            date = date,
                            imageUri = imageUri?.toString(),
                            userName = profile.user_name
                        )

                        // Update workout in database
                        withContext(Dispatchers.IO) {
                            viewModel.updateWorkout(updatedWorkout)
                        }
                        findNavController().navigateUp()
                    }
                }
            } ?: run {
                Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Check for gallery permission
    private fun checkGalleryPermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestGalleryPermissionLauncher.launch(permission)
        } else {
            openGallery()
        }
    }

    // Check for camera permission
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            openCamera()
        }
    }

    // Open gallery to select an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    // Open camera to take a picture
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        lifecycleScope.launch {
                            val copiedUri = withContext(Dispatchers.IO) { copyImageToInternalStorage(selectedImageUri) }
                            if (copiedUri != null) {
                                binding.imageViewWorkout.setImageURI(copiedUri)
                                binding.imageContainer.visibility = View.VISIBLE
                                imageUri = copiedUri
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.failed_to_copy_image), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val photo: Bitmap? = data?.extras?.get("data") as? Bitmap
                    if (photo != null) {
                        lifecycleScope.launch {
                            val tempUri: Uri? = withContext(Dispatchers.IO) { getImageUri(requireContext(), photo) }
                            if (tempUri != null) {
                                binding.imageViewWorkout.setImageURI(tempUri)
                                binding.imageContainer.visibility = View.VISIBLE
                                imageUri = tempUri
                            }
                        }
                    }
                }
            }
        }
    }

    // Copy selected image to internal storage
    private suspend fun copyImageToInternalStorage(uri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            val context = requireContext()
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.filesDir, "${UUID.randomUUID()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                Uri.fromFile(file)
            } catch (e: IOException) {
                null
            }
        }
    }

    // Save the bitmap image to internal storage and return its URI
    private suspend fun getImageUri(context: Context, image: Bitmap): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${UUID.randomUUID()}.jpg")
                val outputStream = FileOutputStream(file)
                image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                Uri.fromFile(file)
            } catch (e: IOException) {
                null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 102
        private const val CAMERA_REQUEST_CODE = 103
    }
}
