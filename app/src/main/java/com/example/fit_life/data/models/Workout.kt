package com.example.fit_life.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey val id: String,         // Primary key for the workout entity
    val type: String,                   // Type of workout (e.g., running, cycling)
    val duration: Int,                  // Duration of the workout in minutes
    val caloriesBurned: Int,            // Calories burned during the workout
    val date: String,                   // Date of the workout
    val imageUri: String?,              // Optional URI for an image related to the workout
    val userName: String                // Username of the user who performed the workout
)
