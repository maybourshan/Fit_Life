package com.example.fit_life.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fit_life.data.models.Meal
import com.example.fit_life.data.repositories.MealRepository
import com.example.fit_life.database.FitLifeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MealRepository
    private val _mealList = MutableLiveData<List<Meal>>()
    val mealList: LiveData<List<Meal>> get() = _mealList

    init {
        val mealDao = FitLifeDatabase.getDatabase(application).mealDao()
        repository = MealRepository(mealDao)
    }

    // Adds a new meal to the repository and reloads the meals.
    fun addMeal(meal: Meal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMeal(meal)
            loadMeals(meal.userName)
        }
    }

    // Updates an existing meal in the repository and reloads the meals.
    fun updateMeal(meal: Meal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMeal(meal)
            loadMeals(meal.userName)
        }
    }

    // Deletes a meal from the repository and reloads the meals.
    fun deleteMeal(meal: Meal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeal(meal)
            loadMeals(meal.userName)
        }
    }

    // Loads the meals for a specific user.
    fun loadMeals(userName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val meals = repository.getMealsForUser(userName)
            withContext(Dispatchers.Main) {
                _mealList.value = meals
            }
        }
    }

    // Returns live data of a specific meal by its ID.
    fun getMealLiveData(mealId: String): MutableLiveData<Meal?> {
        val mealLiveData = MutableLiveData<Meal?>()
        viewModelScope.launch(Dispatchers.IO) {
            val meal = repository.getMealById(mealId)
            withContext(Dispatchers.Main) {
                mealLiveData.value = meal
            }
        }
        return mealLiveData
    }

    // Gets a specific meal by its ID.
    suspend fun getMealById(mealId: String): Meal? {
        return withContext(Dispatchers.IO) {
            repository.getMealById(mealId)
        }
    }
}