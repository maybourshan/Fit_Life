package com.example.fit_life.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// Define the Meal data class as an entity in the Room database
@Entity(tableName = "meals")
data class Meal(
    // Define the primary key for the entity
    @PrimaryKey val id: String,
    val name: String,     // Define the name of the meal
    val calories: Int,   // Define the number of calories in the meal
    val date: String,   // Define the date the meal was added
    val imageUri: String?,    // Define the URI for the meal's image (nullable)
    val userName: String    // Define the username of the user who added the meal

)
