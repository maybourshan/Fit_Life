package com.example.fit_life.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.fit_life.R
import com.example.fit_life.data.models.Workout
import com.example.fit_life.data.repositories.WorkoutRepository
import com.example.fit_life.data.models.Meal
import com.example.fit_life.data.repositories.MealRepository
import com.example.fit_life.database.FitLifeDatabase
import com.example.fit_life.utils.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val workoutRepository: WorkoutRepository
    private val mealRepository: MealRepository

    private val _workoutList = MutableLiveData<List<Workout>>()
    val workoutList: LiveData<List<Workout>> get() = _workoutList

    private val _mealList = MutableLiveData<List<Meal>>()
    val mealList: LiveData<List<Meal>> get() = _mealList

    val currentViewType = MutableLiveData<ViewType>()

    init {
        val workoutDao = FitLifeDatabase.getDatabase(application).workoutDao()
        val mealDao = FitLifeDatabase.getDatabase(application).mealDao()
        workoutRepository = WorkoutRepository(workoutDao)
        mealRepository = MealRepository(mealDao)

        // Load workouts and meals data
        loadWorkouts()
        loadMeals()
    }

    private fun loadWorkouts() {
        UserSessionManager.getCurrentUserProfile(getApplication()).observeForever { userProfile ->
            userProfile?.let { profile ->
                viewModelScope.launch(Dispatchers.IO) {
                    val workouts = workoutRepository.getWorkoutsForUser(profile.user_name)
                    withContext(Dispatchers.Main) {
                        _workoutList.value = workouts
                    }
                }
            }
        }
    }

    private fun loadMeals() {
        UserSessionManager.getCurrentUserProfile(getApplication()).observeForever { userProfile ->
            userProfile?.let { profile ->
                viewModelScope.launch(Dispatchers.IO) {
                    val meals = mealRepository.getMealsForUser(profile.user_name)
                    withContext(Dispatchers.Main) {
                        _mealList.value = meals
                    }
                }
            }
        }
    }

    // Function to get weekly workout data
    fun getWeeklyData(workouts: List<Workout>): Map<String, Pair<Int, Int>> {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfWeek = calendar.time

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weeklyData = mutableMapOf<String, Pair<Int, Int>>()

        val daysOfWeek = getApplication<Application>().resources.getStringArray(R.array.days_of_week)

        daysOfWeek.forEach { day ->
            weeklyData[day] = Pair(0, 0)
        }

        workouts.forEach { workout ->
            val workoutDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(workout.date)
            if (workoutDate != null && workoutDate >= startOfWeek && workoutDate <= endOfWeek) {
                val dayOfWeek = daysOfWeek[workoutDate.day]
                val currentData = weeklyData[dayOfWeek] ?: Pair(0, 0)
                weeklyData[dayOfWeek] = Pair(currentData.first + workout.duration, currentData.second + workout.caloriesBurned)
            }
        }

        return weeklyData.toSortedMap(compareBy { dayOfWeekOrder(it) })
    }

    // Function to get monthly workout data
    fun getMonthlyData(workouts: List<Workout>): Map<String, Pair<Int, Int>> {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.add(Calendar.MONTH, -3)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfLast4Months = calendar.time

        val monthlyData = mutableMapOf<String, Pair<Int, Int>>()
        val months = getApplication<Application>().resources.getStringArray(R.array.months)

        for (i in 3 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            val monthName = months[(calendar.get(Calendar.MONTH) + 12) % 12]
            monthlyData[monthName] = Pair(0, 0)
        }

        workouts.forEach { workout ->
            val workoutDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(workout.date)
            if (workoutDate != null && workoutDate >= startOfLast4Months) {
                calendar.time = workoutDate
                val monthName = months[calendar.get(Calendar.MONTH)]
                val currentData = monthlyData[monthName] ?: Pair(0, 0)
                monthlyData[monthName] = Pair(currentData.first + workout.duration, currentData.second + workout.caloriesBurned)
            }
        }

        return monthlyData.toSortedMap(compareBy { months.indexOf(it) })
    }

    // Function to get yearly workout data
    fun getYearlyData(workouts: List<Workout>): Map<String, Pair<Int, Int>> {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.add(Calendar.YEAR, -3) // Last 4 years
        calendar.set(Calendar.DAY_OF_YEAR, 1) // Set to the first day of the year
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfLast4Years = calendar.time

        val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val yearlyData = mutableMapOf<String, Pair<Int, Int>>()

        // Initialize the last 4 years in the map
        for (i in 0..3) {
            val year = dateFormat.format(calendar.time)
            yearlyData[year] = Pair(0, 0)
            calendar.add(Calendar.YEAR, 1)
        }

        workouts.forEach { workout ->
            val workoutDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(workout.date)
            if (workoutDate != null && workoutDate >= startOfLast4Years) {
                val year = dateFormat.format(workoutDate)
                if (yearlyData.containsKey(year)) {
                    val currentData = yearlyData[year] ?: Pair(0, 0)
                    yearlyData[year] = Pair(currentData.first + workout.duration, currentData.second + workout.caloriesBurned)
                }
            }
        }

        return yearlyData.toSortedMap()
    }

    // Function to get weekly meal data
    fun getWeeklyMealData(meals: List<Meal>): Map<String, Int> {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfWeek = calendar.time

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weeklyData = mutableMapOf<String, Int>()

        val daysOfWeek = getApplication<Application>().resources.getStringArray(R.array.days_of_week)

        daysOfWeek.forEach { day ->
            weeklyData[day] = 0
        }

        meals.forEach { meal ->
            val mealDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(meal.date)
            if (mealDate != null && mealDate >= startOfWeek && mealDate <= endOfWeek) {
                val dayOfWeek = daysOfWeek[mealDate.day]
                val currentData = weeklyData[dayOfWeek] ?: 0
                weeklyData[dayOfWeek] = currentData + meal.calories
            }
        }

        return weeklyData.toSortedMap(compareBy { dayOfWeekOrder(it) })
    }

    // Function to get monthly meal data
    fun getMonthlyMealData(meals: List<Meal>): Map<String, Int> {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.add(Calendar.MONTH, -3)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfLast4Months = calendar.time

        val monthlyData = mutableMapOf<String, Int>()
        val months = getApplication<Application>().resources.getStringArray(R.array.months)

        for (i in 3 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            val monthName = months[(calendar.get(Calendar.MONTH) + 12) % 12]
            monthlyData[monthName] = 0
        }

        meals.forEach { meal ->
            val mealDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(meal.date)
            if (mealDate != null && mealDate >= startOfLast4Months) {
                calendar.time = mealDate
                val monthName = months[calendar.get(Calendar.MONTH)]
                val currentData = monthlyData[monthName] ?: 0
                monthlyData[monthName] = currentData + meal.calories
            }
        }

        return monthlyData.toSortedMap(compareBy { months.indexOf(it) })
    }

    // Function to get yearly meal data
    fun getYearlyMealData(meals: List<Meal>): Map<String, Int> {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.add(Calendar.YEAR, -3) // Last 4 years
        calendar.set(Calendar.DAY_OF_YEAR, 1) // Set to the first day of the year
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfLast4Years = calendar.time

        val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val yearlyData = mutableMapOf<String, Int>()

        // Initialize the last 4 years in the map
        for (i in 0..3) {
            val year = dateFormat.format(calendar.time)
            yearlyData[year] = 0
            calendar.add(Calendar.YEAR, 1)
        }

        meals.forEach { meal ->
            val mealDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(meal.date)
            if (mealDate != null && mealDate >= startOfLast4Years) {
                val year = dateFormat.format(mealDate)
                if (yearlyData.containsKey(year)) {
                    val currentData = yearlyData[year] ?: 0
                    yearlyData[year] = currentData + meal.calories
                }
            }
        }

        return yearlyData.toSortedMap()
    }

    // Helper function to get the order of the days of the week
    private fun dayOfWeekOrder(day: String): Int {
        val daysOfWeek = getApplication<Application>().resources.getStringArray(R.array.days_of_week)
        return daysOfWeek.indexOf(day)
    }

}

// Enum class for view type
enum class ViewType {
    WEEKLY,
    MONTHLY,
    YEARLY
}
