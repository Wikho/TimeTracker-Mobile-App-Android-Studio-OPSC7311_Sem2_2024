package com.example.opsc7311_sem2_2024.Tasks

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.example.opsc7311_sem2_2024.ValidationManager
import com.example.opsc7311_sem2_2024.databinding.ActivityEditTaskBinding
import com.example.opsc7311_sem2_2024.databinding.DeleteConfirmationBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*

class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding
    private val validationManager = ValidationManager()
    private val firebaseManager = FirebaseManager()

    // Variables for task data
    private var taskId: String? = null
    private var currentTask: TaskItem? = null
    private val selectedCategories = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize View Binding
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the taskId from the intent
        taskId = intent.getStringExtra("taskId")

        // Fetch task from Firebase
        taskId?.let { id ->
            firebaseManager.fetchTasks { tasks ->
                currentTask = tasks.find { it.id == id }
                currentTask?.let { task ->
                    populateUI(task)
                } ?: run {
                    Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // Set up listeners and validations
        setupListeners()


    }

    private fun setupListeners() {
        // Validation TextWatchers
        binding.etTaskTitleEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validationManager.isTextNotEmpty(binding.etTaskTitleEdit, binding.tilTaskTitleEdit)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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

        // Time Picker
        binding.etTaskTime.setOnClickListener {
            showTimePicker { selectedTime ->
                binding.etTaskTime.setText(selectedTime)
            }
        }

        // Date Picker
        binding.etDatePicker.setOnClickListener {
            showDatePicker()
        }

        // Add Chip Button
        binding.btnAddChip.setOnClickListener {
            val tagText = binding.etAddChip.text.toString().trim().uppercase()
            if (tagText.isNotEmpty()) {
                if (!selectedCategories.contains(tagText)) {
                    addChipToGroup(tagText, binding.chipGroupCategory)
                    binding.etAddChip.text?.clear()
                    selectedCategories.add(tagText)
                    // Save new category under the user
                    firebaseManager.saveCategories(selectedCategories.toList())
                } else {
                    Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show()
                    binding.etAddChip.text?.clear()
                }
            }
        }

        // Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Save Task Button
        binding.btnSaveTask.setOnClickListener {
            saveTask()
        }

        // Delete Task Button
        binding.btnDeleteTask.setOnClickListener {
            showDeleteConfirmation {
                deleteTask()
            }
        }

    }

    // Function to populate UI with task data
    private fun populateUI(task: TaskItem) {
        // Set task title
        binding.etTaskTitleEdit.setText(task.title)

        // Set task date
        binding.etDatePicker.setText(task.startDate)

        // Set task time
        val timeNumber = task.time.substringAfter("Time: ").trim()
        binding.etTaskTime.setText(timeNumber)

        // Set min and max target hours
        binding.etMinHours.setText(task.minTargetHours.toString())
        binding.etMaxHours.setText(task.maxTargetHours.toString())

        // Fetch user's categories
        firebaseManager.fetchCategories { userCategories ->
            val userCategoriesSet = userCategories.map { it.uppercase() }.toSet()

            // Fetch categories for this task
            firebaseManager.fetchCategoriesByTaskId(task.id) { taskCategories ->
                runOnUiThread {
                    // Populate category chips
                    binding.chipGroupCategory.removeAllViews()
                    taskCategories.forEach { category ->
                        val categoryUpper = category.uppercase()
                        val categoryToDisplay = if (userCategoriesSet.contains(categoryUpper)) {
                            categoryUpper
                        } else {
                            "UNDEFINED"
                        }
                        addChipToGroup(categoryToDisplay, binding.chipGroupCategory)
                        selectedCategories.add(categoryToDisplay)
                    }
                }
            }
        }
    }

    // Function to create and add a new Chip to the ChipGroup
    private fun addChipToGroup(text: String, chipGroup: ChipGroup) {
        val chip = Chip(this).apply {
            this.text = text.uppercase()
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                chipGroup.removeView(this)
                selectedCategories.remove(text.uppercase())
            }
        }
        chipGroup.addView(chip)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val dateStr = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                binding.etDatePicker.setText(dateStr)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        // Inflate the dialog's custom view
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        // Initialize the TimePicker
        timePicker.setIs24HourView(true)

        // Create the AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Handle Cancel button click
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Handle OK button click
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

            val timeString = String.format("%02d:%02d", hour, minute)
            onTimeSelected(timeString)
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun saveTask() {
        // Validate inputs
        val isTitleValid = validationManager.isTextNotEmpty(binding.etTaskTitleEdit, binding.tilTaskTitleEdit)
        val isCategoryValid = validationManager.isChipGroupNotEmpty(binding.chipGroupCategory, binding.tilAddChip)
        val isMinHoursValid = validationManager.validateMinHours(binding.etMinHours, binding.tilMinHours, binding.etMaxHours)
        val isMaxHoursValid = validationManager.validateMaxHours(binding.etMaxHours, binding.tilMaxHours, binding.etMinHours)
        val isTaskTimeValid = validationManager.validateTaskTime(binding.etTaskTime, binding.tilTaskTime)

        if (isTitleValid && isCategoryValid && isMinHoursValid && isMaxHoursValid && isTaskTimeValid) {
            // Get updated task data from UI elements
            val title = binding.etTaskTitleEdit.text.toString()
            val category = selectedCategories.joinToString(",")

            val startDate = binding.etDatePicker.text.toString()
            val taskTime = "Time: " + binding.etTaskTime.text.toString()
            val minTargetHours = binding.etMinHours.text.toString().toIntOrNull() ?: 0
            val maxTargetHours = binding.etMaxHours.text.toString().toIntOrNull() ?: 0

            // Update the currentTask object
            currentTask?.let { task ->
                task.title = title
                task.category = category
                task.startDate = startDate
                task.time = taskTime
                task.minTargetHours = minTargetHours
                task.maxTargetHours = maxTargetHours

                // Save categories under user
                firebaseManager.saveCategories(selectedCategories.toList())

                // Update the task in Firebase
                firebaseManager.updateTask(task) { success, message ->
                    if (success) {
                        // Notify the user
                        Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()

                        // Save categories under user
                        firebaseManager.saveCategories(selectedCategories.toList())

                        // Close the activity
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmation(onDelete: () -> Unit) {
        val builder = AlertDialog.Builder(this)

        // Inflate the custom dialog layout with binding
        val dialogBinding = DeleteConfirmationBinding.inflate(LayoutInflater.from(this))

        builder.setView(dialogBinding.root)
        val alertDialog = builder.create()

        // Handle the Cancel button using binding
        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss() // Close the dialog
        }

        // Handle the Delete button using binding
        dialogBinding.btnDelete.setOnClickListener {
            onDelete.invoke() // Call the delete callback
            alertDialog.dismiss() // Close the dialog after the delete action
        }

        // Show the dialog
        alertDialog.show()
    }

    private fun deleteTask() {
        currentTask?.let { task ->
            firebaseManager.deleteTask(task.id) { success, message ->
                if (success) {
                    Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent()
                    resultIntent.putExtra("taskDeleted", true)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
