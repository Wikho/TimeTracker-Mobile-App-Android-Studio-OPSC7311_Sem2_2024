package com.example.opsc7311_sem2_2024

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF


class AnalyticsFragment : Fragment() {


    lateinit var pieChart: PieChart



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        // Find PieChart
        pieChart = view.findViewById(R.id.pieChart)
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        // Allow dragging and chart appearance
        pieChart.setDragDecelerationFrictionCoef(0.95f)

        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)

        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        pieChart.setDrawCenterText(true)
        pieChart.setRotationAngle(0f)
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true

        //Animation
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        //Disable text/legend
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        /////////////////////////PIE CHART DATA INSERTED HERE/////////////////////////
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(70f, "Item 1"))
        entries.add(PieEntry(20f, "Item 2"))
        entries.add(PieEntry(10f, "Item 3"))
        /////////////////////////////////////////////////////////////////////////////

        // Create dataSet
        val dataSet = PieDataSet(entries, "Mobile OS")

        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        val colors: ArrayList<Int> = ArrayList()
        colors.add(ContextCompat.getColor(requireContext(), R.color.purple))
        colors.add(ContextCompat.getColor(requireContext(), R.color.green))
        colors.add(ContextCompat.getColor(requireContext(), R.color.red))
        dataSet.colors = colors

        // Create Data and Set to chart
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(15f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.WHITE)
        pieChart.setData(data)
        pieChart.highlightValues(null)

        // Reload chart
        pieChart.invalidate()

        return view
    }

}