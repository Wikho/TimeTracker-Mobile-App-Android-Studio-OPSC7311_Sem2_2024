package com.example.opsc7311_sem2_2024.Analytic

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskDatabase
import com.example.opsc7311_sem2_2024.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var pieChart: PieChart
    private lateinit var taskDatabase: TaskDatabase

    private var fromDate: String? = null
    private var toDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        val view = binding.root

        pieChart = binding.pieChart

        setupPieChart()

        // Initialize the database
        taskDatabase = TaskDatabase.getDatabase(requireContext())

        // Load data and update chart
        lifecycleScope.launch {
            val categoryData = withContext(Dispatchers.IO) {
                aggregateCategoryData()
            }
            updatePieChart(categoryData)
        }

        // <editor-fold desc="Forgot Password Click Listener">

        binding.btnToggleFilters.setOnClickListener {
            val isFilterOff = binding.btnToggleFilters.text == getString(R.string.filter_by_category_off)
            if (isFilterOff) {
                binding.filterContainer.visibility = View.VISIBLE
                binding.btnToggleFilters.text = getString(R.string.filter_by_category_on)
            } else {
                binding.filterContainer.visibility = View.GONE
                binding.btnToggleFilters.text = getString(R.string.filter_by_category_off)
                // Reset filters
                fromDate = null
                toDate = null
                loadDataAndUpdateChart()
            }
        }

        // Date Pickers
        binding.etFromDate.setOnClickListener {
            showDatePicker { date ->
                fromDate = date
                binding.etFromDate.setText(date)
                loadDataAndUpdateChart()
            }
        }

        binding.etToDate.setOnClickListener {
            showDatePicker { date ->
                toDate = date
                binding.etToDate.setText(date)
                loadDataAndUpdateChart()
            }
        }

        // Day, Week, Month Buttons
        binding.btnDayFilter.setOnClickListener {
            setDateRangeForPeriod("day")
            loadDataAndUpdateChart()
        }

        binding.btnWeekFilter.setOnClickListener {
            setDateRangeForPeriod("week")
            loadDataAndUpdateChart()
        }

        binding.btnMonthFilter.setOnClickListener {
            setDateRangeForPeriod("month")
            loadDataAndUpdateChart()
        }

        // </editor-fold>

        return view
    }

    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        pieChart.dragDecelerationFrictionCoef = 0.95f

        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)

        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        pieChart.setDrawCenterText(true)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true

        // Animation
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        // Disable legend
        pieChart.legend.isEnabled = false

        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
    }

    private suspend fun aggregateCategoryData(): Map<String, Int> {
        val taskDao = taskDatabase.taskItemDao()
        val tasks = taskDao.getAllTasks()
        val categoryTimeMap = mutableMapOf<String, Int>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val fromDateParsed = fromDate?.let { dateFormat.parse(it) }
        val toDateParsed = toDate?.let { dateFormat.parse(it) }

        for (task in tasks) {
            for (session in task.sessionHistory) {
                val sessionDate = dateFormat.parse(session.sessionStartDate)
                // Apply date filters
                val isAfterFromDate = fromDateParsed?.let { sessionDate >= it } ?: true
                val isBeforeToDate = toDateParsed?.let { sessionDate <= it } ?: true

                if (isAfterFromDate && isBeforeToDate) {
                    val durationMinutes = calculateSessionDurationInMinutes(session.sessionDuration)
                    val categories = task.category.split(",").map { it.trim() }
                    for (category in categories) {
                        val total = categoryTimeMap.getOrDefault(category, 0)
                        categoryTimeMap[category] = total + durationMinutes
                    }
                }
            }
        }

        return categoryTimeMap
    }

    private fun calculateSessionDurationInMinutes(sessionDuration: String?): Int {
        sessionDuration?.let {
            val parts = it.split(":")
            if (parts.size == 2) {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                return hours * 60 + minutes
            }
        }
        return 0
    }

    private fun updatePieChart(categoryData: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        for ((category, totalMinutes) in categoryData) {
            if (totalMinutes > 0) {
                val timeStr = formatMinutesToHoursAndMinutes(totalMinutes)
                val label = "$category: $timeStr"
                entries.add(PieEntry(totalMinutes.toFloat(), label))
            }
        }

        if (entries.isEmpty()) {
            // No data available
            pieChart.clear()
            pieChart.setNoDataText("No data available")
            pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "Task Categories")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // Set colors for the chart slices
        val colors: ArrayList<Int> = ArrayList()
        val colorTemplates = listOf(
            R.color.purple,
            R.color.green,
            R.color.red,
            R.color.blue,
            R.color.orange,
            R.color.yellow,
            R.color.teal
        )

        for (colorRes in colorTemplates) {
            colors.add(ContextCompat.getColor(requireContext(), colorRes))
        }

        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(15f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.WHITE)

        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }

    private fun formatMinutesToHoursAndMinutes(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) {
            String.format("%d:%02d", hours, minutes)
        } else {
            String.format("%d min", minutes)
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val dateStr = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(dateStr)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun setDateRangeForPeriod(period: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val today = dateFormat.format(calendar.time)

        when (period) {
            "day" -> {
                fromDate = today
                toDate = today
            }
            "week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                fromDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                toDate = dateFormat.format(calendar.time)
            }
            "month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                fromDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                toDate = dateFormat.format(calendar.time)
            }
        }
        binding.etFromDate.setText(fromDate)
        binding.etToDate.setText(toDate)
    }

    private fun loadDataAndUpdateChart() {
        lifecycleScope.launch {
            val categoryData = withContext(Dispatchers.IO) {
                aggregateCategoryData()
            }
            updatePieChart(categoryData)
            updateStatistics(categoryData)
        }
    }

    private fun updateStatistics(categoryData: Map<String, Int>) {
        // Calculate and update Task Completed, Time Tracked, etc.
        // For example:
        val totalTimeTracked = categoryData.values.sum()
        val totalHours = totalTimeTracked / 60
        val totalMinutes = totalTimeTracked % 60
        binding.tvTimeTracked.text = String.format("%d:%02d", totalHours, totalMinutes)
        // Implement calculations for other statistics similarly
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
