package com.example.opsc7311_sem2_2024.Tasks

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.example.opsc7311_sem2_2024.ValidationManager
import com.example.opsc7311_sem2_2024.databinding.FragmentTaskCreationBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*

class TaskCreationFragment : Fragment() {

    private var _binding: FragmentTaskCreationBinding? = null
    private val binding get() = _binding!!
    private val validationManager = ValidationManager()
    private val firebaseManager = FirebaseManager()

    private val selectedCategories = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskCreationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Set up listeners and validations
        binding.etTaskTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validationManager.isTextNotEmpty(binding.etTaskTitle, binding.tilTaskTitle)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etDatePicker.setOnClickListener {
            showDatePicker()
        }

        binding.etTaskTime.setOnClickListener {
            showTimePicker { selectedTime ->
                binding.etTaskTime.setText(selectedTime)
            }
        }

        binding.etMinHours.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validationManager.validateMinHours(binding.etMinHours, binding.tilMinHours, binding.etMaxHours)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etMaxHours.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validationManager.validateMaxHours(binding.etMaxHours, binding.tilMaxHours, binding.etMinHours)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Add Chip button
        binding.btnAddChip.setOnClickListener {
            val categoryText = binding.etAddChip.text.toString().trim().uppercase()
            if (categoryText.isNotEmpty()) {
                if (!selectedCategories.contains(categoryText)) {
                    addChipToGroup(categoryText, binding.chipGroupCategory)
                    binding.etAddChip.text?.clear()
                    selectedCategories.add(categoryText)
                    // Save all selected categories under the user
                    firebaseManager.saveCategories(selectedCategories.toList())
                } else {
                    Toast.makeText(requireContext(), "Category already exists", Toast.LENGTH_SHORT).show()
                    binding.etAddChip.text?.clear()
                }
            }
        }

        // Fetch existing categories and populate chips
        firebaseManager.fetchCategories { categories ->
            activity?.runOnUiThread {
                categories.forEach { category ->
                    val categoryUpper = category.uppercase()
                    if (!selectedCategories.contains(categoryUpper)) {
                        addChipToGroup(categoryUpper, binding.chipGroupCategory)
                        selectedCategories.add(categoryUpper)
                    }
                }
            }
        }


        // Create Task button
        binding.btnCreateTaskPage.setOnClickListener {
            createTask()
        }

        // Discard Draft (Back button)
        binding.fabDiscard.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun addChipToGroup(text: String, chipGroup: ChipGroup) {
        val chip = Chip(requireContext()).apply {
            this.text = text.uppercase()
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                chipGroup.removeView(this)
                selectedCategories.remove(text.uppercase())
            }
        }
        chipGroup.addView(chip)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ObsoleteSdkInt")
    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_time_picker, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        timePicker.setIs24HourView(true)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            val hour = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                timePicker.hour
            } else {
                timePicker.currentHour
            }

            val minute = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                timePicker.minute
            } else {
                timePicker.currentMinute
            }

            val timeString = String.format("%d:%02d", hour, minute)
            onTimeSelected(timeString)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createTask() {
        // Validate inputs
        val isTitleValid = validationManager.isTextNotEmpty(binding.etTaskTitle, binding.tilTaskTitle)
        val isCategoryValid = validationManager.isChipGroupNotEmpty(binding.chipGroupCategory, binding.tilAddChip)
        val isMinHoursValid = validationManager.validateMinHours(binding.etMinHours, binding.tilMinHours, binding.etMaxHours)
        val isMaxHoursValid = validationManager.validateMaxHours(binding.etMaxHours, binding.tilMaxHours, binding.etMinHours)
        val isTaskTimeValid = validationManager.validateTaskTime(binding.etTaskTime, binding.tilTaskTime)
        val isDateValid = validationManager.validateTaskDate(binding.etDatePicker, binding.tilDatePicker)

        if (isTitleValid && isCategoryValid && isMinHoursValid && isMaxHoursValid && isTaskTimeValid && isDateValid) {
            val taskId = UUID.randomUUID().toString()
            val taskTitle = binding.etTaskTitle.text.toString()
            val categories = selectedCategories.joinToString(",")
            val selectedDate = binding.etDatePicker.text.toString()
            val taskTime = binding.etTaskTime.text.toString()
            val minTargetHours = binding.etMinHours.text.toString().toInt()
            val maxTargetHours = binding.etMaxHours.text.toString().toInt()

            val task = TaskItem(
                id = taskId,
                title = taskTitle,
                category = categories,
                startDate = selectedDate,
                time = taskTime,
                minTargetHours = minTargetHours,
                maxTargetHours = maxTargetHours
            )

            // Save categories under user
            firebaseManager.saveCategories(selectedCategories.toList())

            // Save task to Firebase
            firebaseManager.saveTask(task) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "Task created successfully", Toast.LENGTH_SHORT).show()
                    // Notify parent fragment
                    parentFragmentManager.setFragmentResult("taskCreated", Bundle())
                    // Close fragment
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val dateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.etDatePicker.setText(dateStr)
                binding.tilDatePicker.error = null // Clear any previous errors
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the minimum date to today
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerDialog.show()
    }

}
