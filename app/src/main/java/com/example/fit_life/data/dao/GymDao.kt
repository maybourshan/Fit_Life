package com.example.fit_life.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fit_life.data.models.Gym
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
interface GymDao {

    // Inserts multiple gym records into the database, replacing them if there's a conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg gyms: Gym)

    // Retrieves all gyms from the database as a LiveData list
    @Query("SELECT * FROM gym")
    fun getAllGyms(): LiveData<List<Gym>>

    // Deletes all gym records from the database
    @Query("DELETE FROM gym")
    suspend fun deleteAllGyms()
}
