package com.example.fit_life.data.dao

import androidx.room.*
import com.example.fit_life.data.models.Meal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Data Access Object (DAO) interface for Meal entity
@Dao
interface MealDao {

    // Insert a meal into the database. If there's a conflict, replace the existing meal.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    // Update an existing meal in the database
    @Update
    suspend fun updateMeal(meal: Meal)

    // Delete a meal from the database
    @Delete
    suspend fun deleteMeal(meal: Meal)

    // Retrieve all meals for a specific user
    @Query("SELECT * FROM meals WHERE userName = :userName")
    suspend fun getMealsForUser(userName: String): List<Meal>

    // Retrieve a meal by its ID, limiting the result to one
    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    suspend fun getMealById(id: String): Meal?
}
