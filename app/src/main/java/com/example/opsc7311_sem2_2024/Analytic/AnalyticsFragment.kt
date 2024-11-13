package com.example.opsc7311_sem2_2024.Analytics

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.example.opsc7311_sem2_2024.TaskClasses.TaskSession
import com.github.mikephil.charting.utils.ColorTemplate

class AnalyticsFragment : Fragment() {

    // <editor-fold desc="Variables and Initialization">
    private lateinit var btnSelectMode: Button
    private lateinit var btnInfo: ImageButton
    private lateinit var btnToggleFilters: Button
    private lateinit var filterContainer: LinearLayout
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText
    private lateinit var btnDayFilter: Button
    private lateinit var btnWeekFilter: Button
    private lateinit var btnMonthFilter: Button
    private lateinit var timeAllocationButtonsContainer: LinearLayout
    private lateinit var btnCategories: Button
    private lateinit var btnTasks: Button

    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var radarChart: RadarChart

    private lateinit var tvStatistics: TextView

    private val firebaseManager = FirebaseManager()

    private var currentMode: AnalyticsMode = AnalyticsMode.GOAL_TRACKER

    private var allTasksList = mutableListOf<TaskItem>()

    private var fromDate: Date? = null
    private var toDate: Date? = null

    enum class AnalyticsMode {
        GOAL_TRACKER,
        TASK_TRACKER,
        TIME_ALLOCATION,
        GOAL_OVERVIEW
    }

    // </editor-fold>

    // <editor-fold desc="Lifecycle Methods">

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        // Initialize views
        btnSelectMode = view.findViewById(R.id.btnSelectMode)
        btnInfo = view.findViewById(R.id.btnInfo)
        btnToggleFilters = view.findViewById(R.id.btnToggleFilters)
        filterContainer = view.findViewById(R.id.filterContainer)
        etFromDate = view.findViewById(R.id.etFromDate)
        etToDate = view.findViewById(R.id.etToDate)
        btnDayFilter = view.findViewById(R.id.btnDayFilter)
        btnWeekFilter = view.findViewById(R.id.btnWeekFilter)
        btnMonthFilter = view.findViewById(R.id.btnMonthFilter)
        timeAllocationButtonsContainer = view.findViewById(R.id.timeAllocationButtonsContainer)
        btnCategories = view.findViewById(R.id.btnCategories)
        btnTasks = view.findViewById(R.id.btnTasks)

        lineChart = view.findViewById(R.id.lineChart)
        barChart = view.findViewById(R.id.barChart)
        pieChart = view.findViewById(R.id.pieChart)
        radarChart = view.findViewById(R.id.radarChart)

        tvStatistics = view.findViewById(R.id.tvStatistics)

        // Set up listeners
        setupListeners()

        // Load tasks
        loadTasks()

        return view
    }

    // </editor-fold>

    // <editor-fold desc="Setup Listeners">

    private fun setupListeners() {
        btnSelectMode.setOnClickListener {
            showModeSelectionDialog()
        }

        btnInfo.setOnClickListener {
            showInfoDialog()
        }

        btnToggleFilters.setOnClickListener {
            toggleFilters()
        }

        etFromDate.setOnClickListener {
            showDatePickerDialog(true)
        }

        etToDate.setOnClickListener {
            showDatePickerDialog(false)
        }

        btnDayFilter.setOnClickListener {
            setDateRangeToDay()
        }

        btnWeekFilter.setOnClickListener {
            setDateRangeToWeek()
        }

        btnMonthFilter.setOnClickListener {
            setDateRangeToMonth()
        }

        btnCategories.setOnClickListener {
            // Handle Categories button click
            showTimeAllocationChart(true)
        }

        btnTasks.setOnClickListener {
            // Handle Tasks button click
            showTimeAllocationChart(false)
        }
    }

    // </editor-fold>

    // <editor-fold desc="Mode Selection">

    private fun showModeSelectionDialog() {
        val modes = arrayOf("Goal Tracker", "Task Tracker", "Time Allocation", "Goal Overview")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Mode")
        builder.setItems(modes) { dialog, which ->
            when (which) {
                0 -> {
                    currentMode = AnalyticsMode.GOAL_TRACKER
                    btnSelectMode.text = "Goal Tracker"
                    timeAllocationButtonsContainer.visibility = View.GONE
                    loadChartData()
                }
                1 -> {
                    currentMode = AnalyticsMode.TASK_TRACKER
                    btnSelectMode.text = "Task Tracker"
                    timeAllocationButtonsContainer.visibility = View.GONE
                    loadChartData()
                }
                2 -> {
                    currentMode = AnalyticsMode.TIME_ALLOCATION
                    btnSelectMode.text = "Time Allocation"
                    timeAllocationButtonsContainer.visibility = View.VISIBLE
                    loadChartData()
                }
                3 -> {
                    currentMode = AnalyticsMode.GOAL_OVERVIEW
                    btnSelectMode.text = "Goal Overview"
                    timeAllocationButtonsContainer.visibility = View.GONE
                    loadChartData()
                }
            }
        }
        builder.show()
    }

    private fun showInfoDialog() {
        val message = """
            Goal Tracker – Line Chart comparing task time spent per session with min and max work goals.
            Task Tracker – Bar Chart comparing time spent on tasks.
            Time Allocation – Pie Chart showing distribution of time spent on categories or tasks.
            Goal Overview – Radar Chart showing multiple goals or comparing progress across categories at a glance.
        """.trimIndent()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Mode Information")
        builder.setMessage(message)
        builder.setPositiveButton("Close") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    // </editor-fold>

    // <editor-fold desc="Filter Toggle">

    private fun toggleFilters() {
        val isFilterOff = btnToggleFilters.text == "Filter Off"
        if (isFilterOff) {
            filterContainer.visibility = View.VISIBLE
            btnToggleFilters.text = "Filter On"
        } else {
            filterContainer.visibility = View.GONE
            btnToggleFilters.text = "Filter Off"
        }
    }

    // </editor-fold>

    // <editor-fold desc="Date Picker">

    private fun showDatePickerDialog(isFromDate: Boolean) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                if (isFromDate) {
                    etFromDate.setText(dateStr)
                    fromDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                } else {
                    etToDate.setText(dateStr)
                    toDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                }
                loadChartData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // </editor-fold>

    // <editor-fold desc="Date Range Setters">

    private fun setDateRangeToDay() {
        val calendar = Calendar.getInstance()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        etFromDate.setText(dateStr)
        etToDate.setText(dateStr)
        fromDate = calendar.time
        toDate = calendar.time
        loadChartData()
    }

    private fun setDateRangeToWeek() {
        val calendar = Calendar.getInstance()
        // Set to the first day of the week
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startOfWeek = calendar.time
        etFromDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startOfWeek))
        fromDate = startOfWeek

        // Set to the last day of the week
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = calendar.time
        etToDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endOfWeek))
        toDate = endOfWeek

        loadChartData()
    }

    private fun setDateRangeToMonth() {
        val calendar = Calendar.getInstance()
        // Set to the first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = calendar.time
        etFromDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startOfMonth))
        fromDate = startOfMonth

        // Set to the last day of the month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endOfMonth = calendar.time
        etToDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endOfMonth))
        toDate = endOfMonth

        loadChartData()
    }

    // </editor-fold>

    // <editor-fold desc="Load Tasks">

    private fun loadTasks() {
        firebaseManager.fetchTasks { tasks ->
            activity?.runOnUiThread {
                allTasksList.clear()
                allTasksList.addAll(tasks)
                loadChartData()
            }
        }
    }

    // </editor-fold>

    // <editor-fold desc="Load Chart Data">

    private fun loadChartData() {
        when (currentMode) {
            AnalyticsMode.GOAL_TRACKER -> {
                showLineChart()
            }
            AnalyticsMode.TASK_TRACKER -> {
                showBarChart()
            }
            AnalyticsMode.TIME_ALLOCATION -> {
                // Show Pie Chart based on Categories or Tasks
                showTimeAllocationChart(isCategory = true)
            }
            AnalyticsMode.GOAL_OVERVIEW -> {
                showRadarChart()
            }
        }
    }

    // </editor-fold>

    // <editor-fold desc="Show Line Chart">

    private fun showLineChart() {
        // Hide other charts
        lineChart.visibility = View.VISIBLE
        barChart.visibility = View.GONE
        pieChart.visibility = View.GONE
        radarChart.visibility = View.GONE

        // Prepare data
        val entries = mutableListOf<Entry>()
        val minEntries = mutableListOf<Entry>()
        val maxEntries = mutableListOf<Entry>()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var index = 0f

        val sessions = mutableListOf<Pair<TaskSession, TaskItem>>()
        for (task in allTasksList) {
            // Filter by date range if applicable
            val taskDate = dateFormat.parse(task.startDate)
            if (fromDate != null && toDate != null) {
                if (taskDate.before(fromDate) || taskDate.after(toDate)) {
                    continue
                }
            }
            for (session in task.sessionHistory) {
                sessions.add(Pair(session, task))
            }
        }

        sessions.sortBy { it.first.sessionStartDate }

        for ((session, task) in sessions) {
            val durationInMinutes = calculateDurationInMinutes(session.sessionDuration)
            entries.add(Entry(index, durationInMinutes.toFloat()))
            minEntries.add(Entry(index, (task.minTargetHours * 60).toFloat()))
            maxEntries.add(Entry(index, (task.maxTargetHours * 60).toFloat()))
            index += 1f
        }

        val dataSet = LineDataSet(entries, "Time Spent")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.gray)
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        val minDataSet = LineDataSet(minEntries, "Min Goal")
        minDataSet.color = ContextCompat.getColor(requireContext(), R.color.green)
        minDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        val maxDataSet = LineDataSet(maxEntries, "Max Goal")
        maxDataSet.color = ContextCompat.getColor(requireContext(), R.color.red)
        maxDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        val lineData = LineData(dataSet, minDataSet, maxDataSet)
        lineChart.data = lineData
        lineChart.invalidate()

        // Update statistics
        updateStatisticsForGoalTracker(entries, minEntries, maxEntries)
    }


    // </editor-fold>

    // <editor-fold desc="Show Bar Chart">

    private fun showBarChart() {
        // Hide other charts
        lineChart.visibility = View.GONE
        barChart.visibility = View.VISIBLE
        pieChart.visibility = View.GONE
        radarChart.visibility = View.GONE

        // Prepare data
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        var index = 0f

        for (task in allTasksList) {
            // Filter by date range if applicable
            val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(task.startDate)
            if (fromDate != null && toDate != null) {
                if (taskDate.before(fromDate) || taskDate.after(toDate)) {
                    continue
                }
            }
            val totalDuration = task.getTotalSessionDurationInMinutes().toFloat()
            entries.add(BarEntry(index, totalDuration))
            labels.add(task.title)
            index += 1f
        }

        val dataSet = BarDataSet(entries, "Tasks")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.blue)
        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        barChart.data = barData

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        barChart.setFitBars(true)
        barChart.invalidate()

        // Update statistics
        updateStatisticsForTaskTracker(entries)
    }

    // </editor-fold>

    // <editor-fold desc="Show Pie Chart">

    private fun showTimeAllocationChart(isCategory: Boolean) {
        // Hide other charts
        lineChart.visibility = View.GONE
        barChart.visibility = View.GONE
        pieChart.visibility = View.VISIBLE
        radarChart.visibility = View.GONE

        // Prepare data
        val entries = mutableListOf<PieEntry>()
        val dataMap = mutableMapOf<String, Float>()

        if (isCategory) {
            // Group by category
            for (task in allTasksList) {
                // Filter by date range if applicable
                val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(task.startDate)
                if (fromDate != null && toDate != null) {
                    if (taskDate.before(fromDate) || taskDate.after(toDate)) {
                        continue
                    }
                }
                val categories = task.category.split(",").map { it.trim() }
                val totalDuration = task.getTotalSessionDurationInMinutes().toFloat()
                for (category in categories) {
                    dataMap[category] = dataMap.getOrDefault(category, 0f) + totalDuration
                }
            }
        } else {
            // Group by tasks
            for (task in allTasksList) {
                // Filter by date range if applicable
                val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(task.startDate)
                if (fromDate != null && toDate != null) {
                    if (taskDate.before(fromDate) || taskDate.after(toDate)) {
                        continue
                    }
                }
                val totalDuration = task.getTotalSessionDurationInMinutes().toFloat()
                dataMap[task.title] = dataMap.getOrDefault(task.title, 0f) + totalDuration
            }
        }

        for ((key, value) in dataMap) {
            entries.add(PieEntry(value, key))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val pieData = PieData(dataSet)
        pieData.setValueTextSize(12f)
        pieChart.data = pieData
        pieChart.invalidate()

        // Update statistics
        updateStatisticsForTimeAllocation(dataMap)
    }

    // </editor-fold>

    // <editor-fold desc="Show Radar Chart">

    private fun showRadarChart() {
        // Hide other charts
        lineChart.visibility = View.GONE
        barChart.visibility = View.GONE
        pieChart.visibility = View.GONE
        radarChart.visibility = View.VISIBLE

        // Prepare data
        val entries = mutableListOf<RadarEntry>()
        val labels = mutableListOf<String>()

        for (task in allTasksList) {
            // Filter by date range if applicable
            val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(task.startDate)
            if (fromDate != null && toDate != null) {
                if (taskDate.before(fromDate) || taskDate.after(toDate)) {
                    continue
                }
            }
            val totalDuration = task.getTotalSessionDurationInMinutes().toFloat()
            entries.add(RadarEntry(totalDuration))
            labels.add(task.title)
        }

        val dataSet = RadarDataSet(entries, "Tasks")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.purple)
        dataSet.fillColor = ContextCompat.getColor(requireContext(), R.color.blue)
        dataSet.setDrawFilled(true)
        val radarData = RadarData(dataSet)

        val xAxis = radarChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        radarChart.data = radarData
        radarChart.invalidate()

        // Update statistics
        updateStatisticsForGoalOverview(entries)
    }

    // </editor-fold>

    // <editor-fold desc="Update Statistics">

    private fun updateStatisticsForGoalTracker(
        entries: List<Entry>,
        minEntries: List<Entry>,
        maxEntries: List<Entry>
    ) {
        val avgTimeSpent = entries.map { it.y.toDouble() }.average()
        val avgMinGoal = minEntries.map { it.y.toDouble() }.average()
        val avgMaxGoal = maxEntries.map { it.y.toDouble() }.average()

        val statsText = """
        Average Time Spent: ${formatMinutes(avgTimeSpent.toFloat())}
        Average Min Goal: ${formatMinutes(avgMinGoal.toFloat())}
        Average Max Goal: ${formatMinutes(avgMaxGoal.toFloat())}
    """.trimIndent()

        tvStatistics.text = statsText
    }

    private fun updateStatisticsForTaskTracker(entries: List<BarEntry>) {
        val totalTime = entries.map { it.y }.sum()
        val avgTime = entries.map { it.y }.average()
        val minTime = entries.map { it.y }.minOrNull() ?: 0f
        val maxTime = entries.map { it.y }.maxOrNull() ?: 0f

        val statsText = """
            Total Time Tracked: ${formatMinutes(totalTime)}
            Average Task Duration: ${formatMinutes(avgTime.toFloat())}
            Minimum Task Duration: ${formatMinutes(minTime)}
            Maximum Task Duration: ${formatMinutes(maxTime)}
        """.trimIndent()

        tvStatistics.text = statsText
    }

    private fun updateStatisticsForTimeAllocation(dataMap: Map<String, Float>) {
        val totalTime = dataMap.values.sum()
        val statsText = "Total Time Allocated: ${formatMinutes(totalTime)}"
        tvStatistics.text = statsText
    }

    private fun updateStatisticsForGoalOverview(entries: List<RadarEntry>) {
        val avgTime = entries.map { it.value }.average()
        val minTime = entries.map { it.value }.minOrNull() ?: 0f
        val maxTime = entries.map { it.value }.maxOrNull() ?: 0f

        val statsText = """
            Average Time: ${formatMinutes(avgTime.toFloat())}
            Minimum Time: ${formatMinutes(minTime)}
            Maximum Time: ${formatMinutes(maxTime)}
        """.trimIndent()

        tvStatistics.text = statsText
    }

    // </editor-fold>

    // <editor-fold desc="Helper Functions">

    private fun calculateDurationInMinutes(duration: String?): Int {
        duration?.let {
            val parts = it.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                return hours * 60 + minutes
            }
        }
        return 0
    }

    private fun formatMinutes(minutes: Float): String {
        val hrs = (minutes / 60).toInt()
        val mins = (minutes % 60).toInt()
        return String.format("%02d:%02d", hrs, mins)
    }

    // </editor-fold>
}
