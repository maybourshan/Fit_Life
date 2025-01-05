package com.example.fit_life.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.room.Entity
import androidx.room.PrimaryKey

@Parcelize
@Entity
data class Gym(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Primary key for the Gym entity, auto-generated
    val name: String,                                  // Name of the gym
    val latitude: Double,                              // Latitude coordinate of the gym
    val longitude: Double,                             // Longitude coordinate of the gym
    val address: String                                // Address of the gym
) : Parcelable                                         // Parcelable implementation to pass Gym objects between components