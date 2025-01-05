package com.example.fit_life.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fit_life.data.models.UserProfile

@Dao
interface UserProfileDao {

    // Insert a new user profile into the database, replacing any conflicting entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userProfile: UserProfile)

    // Update an existing user profile in the database
    @Update
    suspend fun update(userProfile: UserProfile)

    // Retrieve a user profile by its username, limiting the result to one entry
    @Query("SELECT * FROM user_profiles WHERE user_name = :userName LIMIT 1")
    suspend fun getUserProfileByUserName(userName: String): UserProfile?

    // Retrieve the current user profile, limiting the result to one entry
    @Query("SELECT * FROM user_profiles WHERE current = 1 LIMIT 1")
    suspend fun getCurrentUserProfile(): UserProfile?

    // Retrieve the current user profile as LiveData, limiting the result to one entry
    @Query("SELECT * FROM user_profiles WHERE current = 1 LIMIT 1")
    fun getCurrentUserProfileLive(): LiveData<UserProfile?>  // הוספת LiveData

    // Reset all user profiles' current flag to false
    @Query("UPDATE user_profiles SET current = 0")
    suspend fun resetCurrentUser()

    // Set a specific user profile as the current user
    @Query("UPDATE user_profiles SET current = 1 WHERE user_name = :userName")
    suspend fun setCurrentUser(userName: String)
}
