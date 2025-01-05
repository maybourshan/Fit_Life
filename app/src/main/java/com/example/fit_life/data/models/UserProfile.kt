package com.example.fit_life.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    var name: String,           // User's name
    var age: Int,               // User's age
    var weight: Float,          // User's weight
    var height: Float,          // User's height
    var fitnessGoal: String,    // User's fitness goal
    @PrimaryKey val user_name: String,  // Updated field name to user_name
    var password: String,       // User's password
    var current: Boolean        // Boolean flag to indicate if this is the current user
)
