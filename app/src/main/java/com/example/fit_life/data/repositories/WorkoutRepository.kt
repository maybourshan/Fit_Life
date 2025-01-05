package com.example.fit_life.data.repositories

import com.example.fit_life.data.dao.WorkoutDao
import com.example.fit_life.data.models.Workout

class WorkoutRepository(private val workoutDao: WorkoutDao) {

     //Adds a workout to the database.
    suspend fun addWorkout(workout: Workout) {
        workoutDao.insertWorkout(workout)
    }


     //Updates an existing workout in the database.
    suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout)
    }

    //Deletes a workout from the database.
    suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout)
    }


    //Fetches all workouts for a specific user from the database.
    suspend fun getWorkoutsForUser(userName: String): List<Workout> {
        return workoutDao.getWorkoutsForUser(userName)
    }


     //Fetches a workout by its ID.
    suspend fun getWorkoutById(id: String): Workout? {
        return workoutDao.getWorkoutById(id)
    }
}
