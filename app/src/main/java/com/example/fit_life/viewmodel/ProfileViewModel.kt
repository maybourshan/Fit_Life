package com.example.fit_life.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.fit_life.database.FitLifeDatabase
import com.example.fit_life.data.models.UserProfile
import com.example.fit_life.data.repositories.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userProfileDao = FitLifeDatabase.getDatabase(application).userProfileDao()
    private val repository: UserProfileRepository = UserProfileRepository(userProfileDao)

    val currentUserProfile: LiveData<UserProfile?> = repository.currentUserProfile

    // Insert a new user profile into the database
    fun insert(userProfile: UserProfile) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(userProfile)
    }

    // Update an existing user profile in the database
    fun updateUser(userProfile: UserProfile) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(userProfile)
    }

    // Retrieve a user profile by its username
    suspend fun getUserProfileByUserName(userName: String): UserProfile? {
        return withContext(Dispatchers.IO) {
            repository.getUserProfileByUserName(userName)
        }
    }

    // Set a specific user profile as the current user
    fun setCurrentUser(userName: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.setCurrentUser(userName)
    }

    // Retrieve the current user profile
    suspend fun getCurrentUserProfile(): UserProfile? {
        return withContext(Dispatchers.IO) {
            repository.getCurrentUserProfile()
        }
    }
}
