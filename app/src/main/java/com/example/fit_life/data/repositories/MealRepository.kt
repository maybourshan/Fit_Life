package com.example.fit_life.data.repositories

import com.example.fit_life.data.dao.MealDao
import com.example.fit_life.data.models.Meal

class MealRepository(private val mealDao: MealDao) {

    // Function to add a meal to the database
    suspend fun addMeal(meal: Meal) {
        mealDao.insertMeal(meal)
    }

    // Function to update an existing meal in the database
    suspend fun updateMeal(meal: Meal) {
        mealDao.updateMeal(meal)
    }

    // Function to delete a meal from the database
    suspend fun deleteMeal(meal: Meal) {
        mealDao.deleteMeal(meal)
    }

    // Function to retrieve all meals for a specific user
    suspend fun getMealsForUser(userName: String): List<Meal> {
        return mealDao.getMealsForUser(userName)
    }

    // Function to retrieve a meal by its ID
    suspend fun getMealById(id: String): Meal? {
        return mealDao.getMealById(id)
    }
}
