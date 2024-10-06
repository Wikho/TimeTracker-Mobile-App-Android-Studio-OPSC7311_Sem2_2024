package com.example.opsc7311_sem2_2024

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.example.opsc7311_sem2_2024.databinding.ActivityEditTaskBinding
import com.example.opsc7311_sem2_2024.databinding.DeleteConfirmationBinding
import java.util.Calendar
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TimePicker
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding

    // Variables for task data
    private var taskId: String? = null
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskDatabase: TaskDatabase
    private var currentTask: TaskItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize View Binding
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the taskId from the intent
        taskId = intent.getStringExtra("taskId")

        // Initialize the database, DAO, repository, and ViewModel
        taskDatabase = TaskDatabase.getDatabase(this)
        val taskDao = taskDatabase.taskItemDao()
        taskRepository = TaskRepository(taskDao)
        val factory = TaskViewModelFactory(taskRepository)
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        // Fetch task data
        taskId?.let { id ->
            taskViewModel.getTaskById(id) { task ->
                if (task != null) {
                    currentTask = task
                    populateUI(task)
                } else {
                    Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up the Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Set up the Add Chip button
        binding.btnAddChip.setOnClickListener {
            val tagText = binding.etAddChip.text.toString()
            if (tagText.isNotEmpty()) {
                addChipToGroup(tagText, binding.chipGroupCategory)
                binding.etAddChip.text?.clear()
            }
        }

        //Show DatePicker Pop Up
        binding.etDatePicker.setOnClickListener {
            showDatePicker(it)
        }

        //Save button
        binding.btnSaveTask.setOnClickListener {
            saveTask()
        }

        binding.etTaskTime.setOnClickListener {
            showTimePicker { selectedTime ->
                binding.etTaskTime.setText(selectedTime)
            }
        }

        binding.btnDeleteTask.setOnClickListener {
            showDeleteConfirmation {
                // Call your delete function here to delete the task
                deleteTask(taskId.toString())
            }
        }

    }

    // Function to populate UI with task data
    private fun populateUI(task: TaskItem) {
        // Set task title
        binding.etTaskTitleEdit.setText(task.title)

        // Populate category chips
        binding.chipGroupCategory.removeAllViews()
        val categories = task.category.split(",")
        categories.forEach { category ->
            addChipToGroup(category.trim(), binding.chipGroupCategory)
        }

        // Set task date
        binding.etDatePicker.setText(task.startDate)

        // Set task time
        val timeNumber = task.time.substringAfter("Time: ").trim()
        binding.etTaskTime.setText(timeNumber)

        // Set min and max target hours
        binding.etMinHours.setText(task.minTargetHours.toString())
        binding.etMaxHours.setText(task.maxTargetHours.toString())

    }

    // Function to create and add a new Chip to the ChipGroup
    private fun addChipToGroup(tagText: String, chipGroup: ChipGroup) {
        val chip = Chip(this).apply {
            text = tagText
            isCloseIconVisible = true  // Show a close icon to allow removal
            setOnCloseIconClickListener { chipGroup.removeView(this) }  // Remove chip on close icon click
        }
        chipGroup.addView(chip)
    }

    //OnCLick event for DatePicker
    private fun showDatePicker(view: View) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val dateStr = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                when (view.id) {
                    R.id.etDatePicker -> {
                        binding.etDatePicker.setText(dateStr)
                    }
                    // Handle other IDs if necessary
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun saveTask() {
        // Get updated task data from UI elements
        val title = binding.etTaskTitleEdit.text.toString()

        // Collect category chips
        val chipTexts = mutableListOf<String>()
        for (i in 0 until binding.chipGroupCategory.childCount) {
            val chip = binding.chipGroupCategory.getChildAt(i) as Chip
            chipTexts.add(chip.text.toString())
        }
        val category = chipTexts.joinToString(separator = ",")

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

            // Update the task in the database
            taskViewModel.updateTask(task) {
                // Notify the user
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()

                // Close the activity
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        // Inflate the dialog's custom view
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        // Initialize the TimePicker
        timePicker.setIs24HourView(false) // Set to true if you prefer 24-hour format

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

    private fun deleteTask(taskId: String) {
        lifecycleScope.launch {
            // Delete the task from the database
            taskViewModel.deleteTask(taskId)

            // Prepare the result intent
            val resultIntent = Intent()
            resultIntent.putExtra("taskDeleted", true)
            setResult(Activity.RESULT_OK, resultIntent)

            // Close the activity
            finish()
        }
    }

}
