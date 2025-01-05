package com.example.fit_life.utils

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.fit_life.database.FitLifeDatabase
import com.example.fit_life.data.models.UserProfile

object UserSessionManager {

    // Get the current user profile as LiveData from the database
    fun getCurrentUserProfile(context: Context): LiveData<UserProfile?> {
        return FitLifeDatabase.getDatabase(context).userProfileDao().getCurrentUserProfileLive()
    }
}
