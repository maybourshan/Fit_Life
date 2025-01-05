package com.example.fit_life.data.dao

import androidx.room.*
import com.example.fit_life.data.models.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Data Access Object (DAO) for Workout entity
@Dao
interface WorkoutDao {

    // Insert a workout into the database. Replace if conflict occurs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    // Update an existing workout in the database
    @Update
    suspend fun updateWorkout(workout: Workout)

    // Delete a workout from the database
    @Delete
    suspend fun deleteWorkout(workout: Workout)

    // Retrieve all workouts for a specific user
    @Query("SELECT * FROM workouts WHERE userName = :userName")
    suspend fun getWorkoutsForUser(userName: String): List<Workout>

    // Retrieve a specific workout by its ID
    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    suspend fun getWorkoutById(id: String): Workout?
}
