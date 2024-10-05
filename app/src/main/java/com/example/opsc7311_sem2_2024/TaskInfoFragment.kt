package com.example.opsc7311_sem2_2024

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import com.example.opsc7311_sem2_2024.databinding.FragmentTaskInfoBinding
import com.google.android.material.chip.Chip
import java.util.Calendar


class TaskInfoFragment : Fragment() {

    // <editor-fold desc="Binding">
    private var _binding: FragmentTaskInfoBinding? = null
    private val binding get() = _binding!!
    // </editor-fold>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskInfoBinding.inflate(inflater, container, false)

        // Retrieve task details from arguments
        val taskTitle = arguments?.getString("taskTitle")

        // Update the UI with the task details
        binding.tvTaskTitle.text = taskTitle

        // Ensure taskCategory is not null, provide default empty string if it is null
        val taskCategory = arguments?.getString("taskCategory") ?: ""
        val tags = taskCategory.split(",")

        tags.forEach { tag ->
            val capitalizedTag = tag.trim().replaceFirstChar { it.uppercase() }
            binding.chipGroupCategory.addView(createChip(capitalizedTag))
        }

        // Ensure taskTime is not null, provide default empty string if it is null
        val taskTime = arguments?.getString("taskTime") ?: ""
        val timeNumber = taskTime.substringAfter("Time: ").trim()

        binding.etTaskTime.text = Editable.Factory.getInstance().newEditable(timeNumber)
        binding.etFromDate.text = Editable.Factory.getInstance().newEditable(timeNumber)
        binding.etToDate.text = Editable.Factory.getInstance().newEditable(timeNumber)

        return binding.root
    }

    // Helper function to create a Chip for the category
    private fun createChip(category: String?): Chip {
        val chip = Chip(context)
        chip.text = category
        return chip
    }

    //Set Click events on buttons
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listener for the task date picker
        binding.etDatePicker.setOnClickListener {
            showDatePicker(binding.etDatePicker) // Show the date picker when clicked
        }

        // Click listener for 'From Date'
        binding.etFromDate.setOnClickListener {
            showDatePicker(binding.etFromDate)
        }

        // Click listener for 'To Date'
        binding.etToDate.setOnClickListener {
            showDatePicker(binding.etToDate)
        }
    }

    //OnCLick event for DatePicker
    private fun showDatePicker(view: View) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        activity?.let {
            val datePickerDialog = DatePickerDialog(
                it,
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                    binding.etDatePicker.setText("$selectedYear-${selectedMonth + 1}-$selectedDay")
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

}