package com.example.opsc7311_sem2_2024

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
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

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var pieChart: PieChart
    private lateinit var taskDatabase: TaskDatabase

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

        for (task in tasks) {
            val categories = task.category.split(",").map { it.trim() }
            for (session in task.sessionHistory) {
                val durationMinutes = calculateSessionDurationInMinutes(session.sessionDuration)
                for (category in categories) {
                    val total = categoryTimeMap.getOrDefault(category, 0)
                    categoryTimeMap[category] = total + durationMinutes
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
