package com.example.fit_life.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.fit_life.MainActivity
import com.example.fit_life.R
import com.example.fit_life.data.models.Meal
import com.example.fit_life.data.models.Workout
import com.example.fit_life.databinding.FragmentProgressBinding
import com.example.fit_life.utils.IntValueFormatter
import com.example.fit_life.viewmodel.ProgressViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ProgressFragment : Fragment() {

    // View binding for accessing UI components
    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    // ViewModel for accessing progress data
    private val viewModel: ProgressViewModel by viewModels()

    private lateinit var barChartWorkoutTime: BarChart
    private lateinit var barChartCaloriesBurned: BarChart
    private lateinit var barChartCaloriesConsumed: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the bar charts
        barChartWorkoutTime = binding.barChartWorkoutTime
        barChartCaloriesBurned = binding.barChartCaloriesBurned
        barChartCaloriesConsumed = binding.barChartCaloriesConsumed

        // Setup charts with common configurations
        setupChart(barChartWorkoutTime)
        setupChart(barChartCaloriesBurned)
        setupChart(barChartCaloriesConsumed)

        // Set up button click listeners to update charts based on the selected time frame
        binding.buttonWeekly.setOnClickListener { updateCharts(getString(R.string.weekly)) }
        binding.buttonMonthly.setOnClickListener { updateCharts(getString(R.string.monthly)) }
        binding.buttonYearly.setOnClickListener { updateCharts(getString(R.string.yearly)) }

        // Observe workout list and update charts and stats accordingly
        viewModel.workoutList.observe(viewLifecycleOwner, Observer { workouts ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    updateTotalStats(workouts, viewModel.mealList.value ?: emptyList(), getString(R.string.weekly)) // Default view
                }
                updateCharts(getString(R.string.weekly)) // Default view
            }
        })

        // Observe meal list and refresh the current view
        viewModel.mealList.observe(viewLifecycleOwner, Observer { meals ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    updateCharts(viewModel.currentViewType.value?.name?.lowercase(Locale.getDefault()) ?: getString(R.string.weekly)) // Refresh current view
                }
            }
        })

        // Handle back button click to navigate to the splash screen
        binding.buttonBackToSplash.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    // Set up the bar chart with common configurations
    private fun setupChart(chart: BarChart) {
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.setDrawGridLines(false)
        chart.xAxis.setDrawGridLines(false)
        chart.axisRight.isEnabled = false

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.labelCount = 7
        chart.xAxis.textSize = 14f
        chart.xAxis.yOffset = 10f

        chart.axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        chart.axisLeft.isEnabled = true
        chart.axisLeft.setDrawLabels(true)
        chart.axisLeft.setDrawAxisLine(true)
        chart.axisLeft.textColor = Color.BLACK
        chart.axisLeft.textSize = 14f
        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.axisLineWidth = 2f // Increase axis line thickness

        chart.xAxis.axisLineWidth = 2f // Increase axis line thickness for X axis
        chart.description.isEnabled = false // Disable the description at the bottom

        // Set offsets to provide more space for the legend
        chart.setExtraOffsets(0f, 0f, 0f, 20f) // Adjust the bottom offset to set the desired space
        chart.legend.textSize = 16f // Adjust this value to set the desired text size
    }

    // Update charts based on the selected time frame
    private fun updateCharts(timeFrame: String) {
        lifecycleScope.launch {
            val workouts = withContext(Dispatchers.IO) { viewModel.workoutList.value ?: emptyList() }
            val meals = withContext(Dispatchers.IO) { viewModel.mealList.value ?: emptyList() }

            val workoutData = when (timeFrame) {
                getString(R.string.weekly) -> viewModel.getWeeklyData(workouts)
                getString(R.string.monthly) -> viewModel.getMonthlyData(workouts)
                getString(R.string.yearly) -> viewModel.getYearlyData(workouts)
                else -> return@launch
            }

            val mealData = when (timeFrame) {
                getString(R.string.weekly) -> viewModel.getWeeklyMealData(meals)
                getString(R.string.monthly) -> viewModel.getMonthlyMealData(meals)
                getString(R.string.yearly) -> viewModel.getYearlyMealData(meals)
                else -> return@launch
            }

            updateTotalStats(workouts, meals, timeFrame)
            updateWorkoutTimeChart(workoutData, timeFrame)
            updateCaloriesBurnedChart(workoutData, timeFrame)
            updateCaloriesConsumedChart(mealData, timeFrame)
        }
    }

    // Update total statistics displayed on the screen
    private suspend fun updateTotalStats(workouts: List<Workout>, meals: List<Meal>, timeFrame: String) {
        var totalWorkouts = 0
        var totalCaloriesBurned = 0
        var totalDuration = 0
        var totalMeals = 0
        var totalCaloriesConsumed = 0

        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        when (timeFrame) {
            getString(R.string.weekly) -> {
                val startOfWeek = Calendar.getInstance().apply {
                    firstDayOfWeek = Calendar.SUNDAY
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val endOfWeek = Calendar.getInstance().apply {
                    firstDayOfWeek = Calendar.SUNDAY
                    set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time

                val weeklyWorkouts = workouts.filter {
                    val workoutDate = dateFormat.parse(it.date)
                    workoutDate != null && workoutDate >= startOfWeek && workoutDate <= endOfWeek
                }
                totalWorkouts = weeklyWorkouts.size
                totalCaloriesBurned = weeklyWorkouts.sumBy { it.caloriesBurned }
                totalDuration = weeklyWorkouts.sumBy { it.duration }

                val weeklyMeals = meals.filter {
                    val mealDate = dateFormat.parse(it.date)
                    mealDate != null && mealDate >= startOfWeek && mealDate <= endOfWeek
                }
                totalMeals = weeklyMeals.size
                totalCaloriesConsumed = weeklyMeals.sumBy { it.calories }
            }
            getString(R.string.monthly) -> {
                val startOfLast4Months = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -3)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val monthlyWorkouts = workouts.filter {
                    val workoutDate = dateFormat.parse(it.date)
                    workoutDate != null && workoutDate >= startOfLast4Months
                }
                totalWorkouts = monthlyWorkouts.size
                totalCaloriesBurned = monthlyWorkouts.sumBy { it.caloriesBurned }
                totalDuration = monthlyWorkouts.sumBy { it.duration }

                val monthlyMeals = meals.filter {
                    val mealDate = dateFormat.parse(it.date)
                    mealDate != null && mealDate >= startOfLast4Months
                }
                totalMeals = monthlyMeals.size
                totalCaloriesConsumed = monthlyMeals.sumBy { it.calories }
            }
            getString(R.string.yearly) -> {
                val startOfLast4Years = Calendar.getInstance().apply {
                    add(Calendar.YEAR, -3)
                    set(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val yearlyWorkouts = workouts.filter {
                    val workoutDate = dateFormat.parse(it.date)
                    workoutDate != null && workoutDate >= startOfLast4Years
                }
                totalWorkouts = yearlyWorkouts.size
                totalCaloriesBurned = yearlyWorkouts.sumBy { it.caloriesBurned }
                totalDuration = yearlyWorkouts.sumBy { it.duration }

                val yearlyMeals = meals.filter {
                    val mealDate = dateFormat.parse(it.date)
                    mealDate != null && mealDate >= startOfLast4Years
                }
                totalMeals = yearlyMeals.size
                totalCaloriesConsumed = yearlyMeals.sumBy { it.calories }
            }
            else -> return
        }

        withContext(Dispatchers.Main) {
            binding.textViewTotalWorkouts.text = getString(R.string.total_workouts_, totalWorkouts)
            binding.textViewTotalCalories.text = getString(R.string.total_calories_burned_, totalCaloriesBurned)
            binding.textViewTotalDuration.text = getString(R.string.total_duration_, totalDuration)
            binding.textViewTotalMeals.text = getString(R.string.total_meals_, totalMeals)
            binding.textViewTotalCaloriesConsumed.text = getString(R.string.total_calories_consumed_, totalCaloriesConsumed)
        }
    }

    private fun updateWorkoutTimeChart(data: Map<String, Pair<Int, Int>>, timeFrame: String) {
        setupChart(barChartWorkoutTime)
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        data.entries.forEachIndexed { index, entry ->
            val totalDuration = entry.value.first
            entries.add(BarEntry(index.toFloat(), totalDuration.toFloat()))
            labels.add(entry.key)
        }

        val dataSet = BarDataSet(entries, getString(R.string.workout_duration)).apply {
            color = Color.parseColor("#8B4513") // Dark brown color
            valueTextSize = 12f // Set text size for values
        }
        val barData = BarData(dataSet)
        barData.setValueFormatter(IntValueFormatter()) // Use custom ValueFormatter

        barChartWorkoutTime.data = barData

        val xAxis = barChartWorkoutTime.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.labelRotationAngle = -45f

        val titleText = when (timeFrame) {
            getString(R.string.weekly) -> getString(R.string.current_week_workout_duration)
            getString(R.string.monthly) -> getString(R.string.last_4_months_workout_duration)
            getString(R.string.yearly) -> getString(R.string.last_4_years_workout_duration)
            else -> ""
        }

        binding.textViewWorkoutDurationTitle.text = titleText
        binding.textViewWorkoutDurationTitle.gravity = Gravity.CENTER_HORIZONTAL // Center the text in the header

        if (Locale.getDefault().language == "iw" || Locale.getDefault().language == "he") {
            binding.textViewWorkoutDurationTitle.layoutDirection = View.LAYOUT_DIRECTION_RTL
            binding.textViewWorkoutDurationTitle.textDirection = View.TEXT_DIRECTION_RTL
        } else {
            binding.textViewWorkoutDurationTitle.layoutDirection = View.LAYOUT_DIRECTION_LTR
            binding.textViewWorkoutDurationTitle.textDirection = View.TEXT_DIRECTION_LTR
        }

        barChartWorkoutTime.invalidate() // refresh
    }

    // Update the calories burned bar chart
    private fun updateCaloriesBurnedChart(data: Map<String, Pair<Int, Int>>, timeFrame: String) {
        setupChart(barChartCaloriesBurned)
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        data.entries.forEachIndexed { index, entry ->
            val totalCalories = entry.value.second
            entries.add(BarEntry(index.toFloat(), totalCalories.toFloat()))
            labels.add(entry.key)
        }

        val dataSet = BarDataSet(entries, getString(R.string.calories_burned)).apply {
            color = Color.parseColor("#8B4513") // Dark brown color
            valueTextSize = 12f // Set text size for values
        }
        val barData = BarData(dataSet)
        barData.setValueFormatter(IntValueFormatter()) // Use custom ValueFormatter

        barChartCaloriesBurned.data = barData
        barChartCaloriesBurned.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChartCaloriesBurned.xAxis.labelRotationAngle = -45f

        binding.textViewCaloriesBurnedTitle.text = when (timeFrame) {
            getString(R.string.weekly) -> getString(R.string.current_week_calories_burned)
            getString(R.string.monthly) -> getString(R.string.last_4_months_calories_burned)
            getString(R.string.yearly) -> getString(R.string.last_4_years_calories_burned)
            else -> ""
        }
        binding.textViewCaloriesBurnedTitle.gravity = Gravity.CENTER_HORIZONTAL //Center the text in the header
        barChartCaloriesBurned.invalidate() // refresh
    }

    // Update the calories consumed bar chart
    private fun updateCaloriesConsumedChart(data: Map<String, Int>, timeFrame: String) {
        setupChart(barChartCaloriesConsumed)
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        data.entries.forEachIndexed { index, entry ->
            val totalCalories = entry.value
            entries.add(BarEntry(index.toFloat(), totalCalories.toFloat()))
            labels.add(entry.key)
        }

        val dataSet = BarDataSet(entries, getString(R.string.calories_consumed)).apply {
            color = Color.parseColor("#8B0000") // Dark red color
            valueTextSize = 12f // Set text size for values
        }
        val barData = BarData(dataSet)
        barData.setValueFormatter(IntValueFormatter()) // Use custom ValueFormatter

        barChartCaloriesConsumed.data = barData
        barChartCaloriesConsumed.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChartCaloriesConsumed.xAxis.labelRotationAngle = -45f

        binding.textViewCaloriesConsumedTitle.text = when (timeFrame) {
            getString(R.string.weekly) -> getString(R.string.current_week_calories_consumed)
            getString(R.string.monthly) -> getString(R.string.last_4_months_calories_consumed)
            getString(R.string.yearly) -> getString(R.string.last_4_years_calories_consumed)
            else -> ""
        }
        barChartCaloriesConsumed.invalidate() // refresh
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding when the view is destroyed
        _binding = null
    }

    // Function to get text based on current locale
    private fun getTextForLocale(englishString: String, hebrewString: String): String {
        val currentLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale
        }

        return when (currentLocale.language) {
            "iw", "he" -> hebrewString
            else -> englishString
        }
    }
}
