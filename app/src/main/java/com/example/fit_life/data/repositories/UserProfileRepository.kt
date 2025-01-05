package com.example.fit_life.data.repositories

import androidx.lifecycle.LiveData
import com.example.fit_life.data.dao.UserProfileDao
import com.example.fit_life.data.models.UserProfile

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    // LiveData representing the current user profile
    val currentUserProfile: LiveData<UserProfile?> = userProfileDao.getCurrentUserProfileLive()

    // Insert a new user profile into the database
    suspend fun insert(userProfile: UserProfile) {
        userProfileDao.insert(userProfile)
    }

    // Update an existing user profile in the database
    suspend fun update(userProfile: UserProfile) {
        userProfileDao.update(userProfile)
    }

    // Retrieve a user profile by its username
    suspend fun getUserProfileByUserName(userName: String): UserProfile? {
        return userProfileDao.getUserProfileByUserName(userName)
    }

    // Set a specific user profile as the current user
    suspend fun setCurrentUser(userName: String) {
        userProfileDao.resetCurrentUser()
        userProfileDao.setCurrentUser(userName)
    }

    // Retrieve the current user profile
    suspend fun getCurrentUserProfile(): UserProfile? {
        return userProfileDao.getCurrentUserProfile()
    }
}
