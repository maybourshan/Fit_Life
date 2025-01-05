package com.example.fit_life.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fit_life.data.models.UserProfile
import com.example.fit_life.data.dao.UserProfileDao
import com.example.fit_life.data.models.Workout
import com.example.fit_life.data.dao.WorkoutDao
import com.example.fit_life.data.models.Meal
import com.example.fit_life.data.dao.MealDao
import com.example.fit_life.data.models.Gym
import com.example.fit_life.data.dao.GymDao

@Database(entities = [UserProfile::class, Workout::class, Meal::class, Gym::class], version = 5, exportSchema = false)
abstract class FitLifeDatabase : RoomDatabase() {

    // Abstract functions to get DAO instances
    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun mealDao(): MealDao
    abstract fun gymDao(): GymDao

    companion object {
        @Volatile
        private var INSTANCE: FitLifeDatabase? = null

        // Singleton pattern to get the database instance
        fun getDatabase(context: Context): FitLifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitLifeDatabase::class.java,
                    "fitlife_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration() // This will reset the database if a migration is missing
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration from version 1 to 2: Add a 'current' column to 'user_profiles' table
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_profiles ADD COLUMN current INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 2 to 3: Placeholder for future schema changes
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Implement the schema change if necessary
            }
        }

        // Migration from version 3 to 4: Create 'meals' and 'gym' tables
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `meals` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `calories` INTEGER NOT NULL, `date` TEXT NOT NULL, `imageUri` TEXT, `userName` TEXT NOT NULL, PRIMARY KEY(`id`))")
                database.execSQL("CREATE TABLE IF NOT EXISTS `gym` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `location` TEXT NOT NULL, `rating` REAL NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        // Migration from version 4 to 5: Placeholder for future schema changes
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add any changes needed for migration from version 4 to 5
            }
        }
    }
}
