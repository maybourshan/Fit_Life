package com.example.fit_life.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fit_life.data.models.Workout
import com.example.fit_life.data.repositories.WorkoutRepository
import com.example.fit_life.database.FitLifeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkoutRepository
    private val _workoutList = MutableLiveData<List<Workout>>()
    val workoutList: LiveData<List<Workout>> get() = _workoutList

    init {
        val workoutDao = FitLifeDatabase.getDatabase(application).workoutDao()
        repository = WorkoutRepository(workoutDao)
    }

    // Adds a new workout to the repository and reloads the workouts.
    fun addWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addWorkout(workout)
            loadWorkouts(workout.userName)
        }
    }

    // Updates an existing workout in the repository and reloads the workouts.
    fun updateWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWorkout(workout)
            loadWorkouts(workout.userName)
        }
    }

    // Deletes a workout from the repository and reloads the workouts.
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorkout(workout)
            loadWorkouts(workout.userName)
        }
    }

    // Loads the workouts for a specific user.
    fun loadWorkouts(userName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val workouts = repository.getWorkoutsForUser(userName)
            withContext(Dispatchers.Main) {
                _workoutList.value = workouts
            }
        }
    }

    // Returns live data of a specific workout by its ID.
    fun getWorkoutLiveData(workoutId: String): MutableLiveData<Workout?> {
        val workoutLiveData = MutableLiveData<Workout?>()
        viewModelScope.launch(Dispatchers.IO) {
            val workout = repository.getWorkoutById(workoutId)
            withContext(Dispatchers.Main) {
                workoutLiveData.value = workout
            }
        }
        return workoutLiveData
    }

    // Gets a specific workout by its ID.
    suspend fun getWorkoutById(workoutId: String): Workout? {
        return withContext(Dispatchers.IO) {
            repository.getWorkoutById(workoutId)
        }
    }
}