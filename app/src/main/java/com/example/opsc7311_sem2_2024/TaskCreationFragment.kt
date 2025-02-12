package com.example.opsc7311_sem2_2024

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.opsc7311_sem2_2024.databinding.FragmentTaskCreationBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class TaskCreationFragment : Fragment() {

    // <editor-fold desc="Binding">
    private var _binding: FragmentTaskCreationBinding? = null
    private val binding get() = _binding!!
    // </editor-fold>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskCreationBinding.inflate(inflater, container, false)

        // <editor-fold desc="Val">
        val chipGroup = binding.chipGroupCategory
        val addChipEditText = binding.etAddChip
        val addButton = binding.btnAddChip
        val validationManager = ValidationManager()
        // </editor-fold>

        // <editor-fold desc="Add Category Button">

        addButton.setOnClickListener {
            val tagText = addChipEditText.text.toString()
            if (tagText.isNotEmpty()) {
                addChipToGroup(tagText, chipGroup)
                addChipEditText.text?.clear()  // Clear the text input after adding the chip
            }
        }

        // </editor-fold>

        // <editor-fold desc="Time Selector">

        binding.etTaskTime.setOnClickListener {
            showTimePicker { selectedTime ->
                binding.etTaskTime.setText(selectedTime)
            }
        }

        binding.etTaskTime.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No use case, Sir
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No use case, Sir
            }

            override fun afterTextChanged(s: Editable?) {
                // Validate task time after text change
                validationManager.validateTaskTime(binding.etTaskTime, binding.tilTaskTime)
            }
        })

        // </editor-fold>

        // <editor-fold desc="Close/Discard FAB">

        binding.fabDiscard.setOnClickListener {
            // Close the current fragment and go back to the previous fragment
            parentFragmentManager.popBackStack()
        }

        // </editor-fold>

        // <editor-fold desc="Task Time, Min and Max onTextChange listeners Checking for mistakes">

        // Add TextWatcher for Task Time
        binding.etTaskTime.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validationManager.validateTaskTime(binding.etTaskTime, binding.tilTaskTime)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Min Hours validation
        binding.etMinHours.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validationManager.validateMinHours(binding.etMinHours, binding.tilMinHours, binding.etMaxHours)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Max Hours validation
        binding.etMaxHours.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validationManager.validateMaxHours(binding.etMaxHours, binding.tilMaxHours, binding.etMinHours)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // </editor-fold>

        // <editor-fold desc="Create Task Button">

        binding.btnCreateTaskPage.setOnClickListener {

            // Validation
            val isTitleValid = validationManager.isTextNotEmpty(binding.etTaskTitle, binding.tilTaskTitle)
            val isCategoryValid = validationManager.isChipGroupNotEmpty(binding.chipGroupCategory, binding.tilAddChip)
            val isMinHoursValid = validationManager.validateMinHours(binding.etMinHours, binding.tilMinHours, binding.etMaxHours)
            val isMaxHoursValid = validationManager.validateMaxHours(binding.etMaxHours, binding.tilMaxHours, binding.etMinHours)
            val isTaskTimeValid = validationManager.validateTaskTime(binding.etTaskTime, binding.tilTaskTime)

            // If all validations are successful
            if (isTitleValid && isCategoryValid && isMinHoursValid && isMaxHoursValid && isTaskTimeValid) {

                // Get task data
                val title = binding.etTaskTitle.text.toString()

                //Category/Chips
// Collect category chips
                val chipTexts = mutableListOf<String>()
                for (i in 0 until binding.chipGroupCategory.childCount) {
                    val chip = binding.chipGroupCategory.getChildAt(i) as Chip
                    chipTexts.add(chip.text.toString().lowercase(Locale.getDefault()))
                }
                val category = chipTexts.joinToString(separator = ",")


                // Get current date and time
                val currentTime = System.currentTimeMillis()
                val creationDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(currentTime))
                val creationTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))

                // Get start date from datePicker
                val calendar = Calendar.getInstance()
                calendar.set(binding.datePicker.year, binding.datePicker.month, binding.datePicker.dayOfMonth)
                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                // Get Min and Max Hours and Task Time
                val taskTime = binding.etTaskTime.text.toString()
                val minTargetHours = binding.etMinHours.text.toString().toInt()
                val maxTargetHours = binding.etMaxHours.text.toString().toInt()

                // Set isStarted and isArchived flags
                val isStarted = false
                val isArchived = false 

                // Initialize sessionHistory as an empty list for now
                val sessionHistory: MutableList<TaskSession> = mutableListOf()

                // Create Task object
                val task = TaskItem(
                    title = title,
                    category = category,
                    time = taskTime,
                    creationDate = creationDate,
                    creationTime = creationTime,
                    startDate = startDate,
                    minTargetHours = minTargetHours,
                    maxTargetHours = maxTargetHours,
                    isStarted = isStarted,
                    isArchived = isArchived,
                    sessionHistory = sessionHistory
                )

                // Get the DAO and insert the task (using coroutines)
                val taskDatabase = TaskDatabase.getDatabase(requireActivity().applicationContext)
                val taskDao = taskDatabase.taskItemDao()

                lifecycleScope.launch {
                    taskDao.insertTask(task)

                    // Notify the parent fragment to reload the RecyclerView
                    parentFragmentManager.setFragmentResult("taskCreated", Bundle())

                    // Show toast for task creation
                    Toast.makeText(requireContext(), "$title Task created", Toast.LENGTH_SHORT).show()

                    // Close the fragment
                    parentFragmentManager.popBackStack()
                }
            }
        }


        // </editor-fold>

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Function to create and add a new Chip to the ChipGroup
    private fun addChipToGroup(tagText: String, chipGroup: ChipGroup) {
        val chip = Chip(requireContext()).apply {
            text = tagText
            isCloseIconVisible = true  // Show a close icon to allow removal
            setOnCloseIconClickListener { chipGroup.removeView(this) }  // Remove chip on close icon click
        }
        chipGroup.addView(chip)
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        // Inflate the dialog's custom view
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_time_picker, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        // Initialize the TimePicker
        timePicker.setIs24HourView(true)

        // Create the AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Handle Cancel button click
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Handle OK button click
        btnOk.setOnClickListener {
            val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.hour
            } else {
                timePicker.currentHour
            }

            val minute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.minute
            } else {
                timePicker.currentMinute
            }

            val timeString = String.format("%d:%02d", hour, minute)
            onTimeSelected(timeString)
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }


}



