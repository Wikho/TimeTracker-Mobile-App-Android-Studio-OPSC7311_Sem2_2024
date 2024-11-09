package com.example.opsc7311_sem2_2024.Tasks

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskDatabase
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.example.opsc7311_sem2_2024.TaskClasses.TaskRepository
import com.example.opsc7311_sem2_2024.TaskClasses.TaskSessionAdapter
import com.example.opsc7311_sem2_2024.TaskClasses.TaskViewModel
import com.example.opsc7311_sem2_2024.TaskClasses.TaskViewModelFactory
import com.example.opsc7311_sem2_2024.databinding.FragmentTaskInfoBinding
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TaskInfoFragment : Fragment() {

    // <editor-fold desc="Binding">
    private var _binding: FragmentTaskInfoBinding? = null
    private val binding get() = _binding!!
    // </editor-fold>

    // <editor-fold desc="Task Get Info Vars">

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskDatabase: TaskDatabase

    private var taskId: String? = null
    private var currentTask: TaskItem? = null
    private var isArchived: Boolean = false

    private lateinit var sessionAdapter: TaskSessionAdapter

    // </editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the task ID from arguments
        taskId = arguments?.getString("taskId")

        // Initialize the database, DAO, repository, and ViewModel
        taskDatabase = TaskDatabase.getDatabase(requireContext())
        val taskDao = taskDatabase.taskItemDao()
        taskRepository = TaskRepository(taskDao)
        val factory = TaskViewModelFactory(taskRepository)
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        //Button Change if Archived
        isArchived = arguments?.getBoolean("isArchived", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskInfoBinding.inflate(inflater, container, false)

        binding.etFromDate.addTextChangedListener(dateFilterWatcher)
        binding.etToDate.addTextChangedListener(dateFilterWatcher)
        binding.seekBarMinDuration.setOnSeekBarChangeListener(durationFilterListener)
        binding.seekBarMaxDuration.setOnSeekBarChangeListener(durationFilterListener)


        return binding.root
    }

    // Fetch the task data in onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch task data
        taskId?.let { id ->
            taskViewModel.getTaskById(id) { task ->
                if (task != null) {
                    currentTask = task
                    populateUI(task)
                } else {
                    Toast.makeText(requireContext(), "Task not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //Recycler view to Show Session History
        setupRecyclerView()

        // Click listener for the back button
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Click listener for the edit button
        binding.btnEditTask.setOnClickListener {
            val intent = Intent(requireContext(), EditTaskActivity::class.java)
            intent.putExtra("taskId", taskId)
            editTaskLauncher.launch(intent)
        }

        // Set click listener for From Date
        binding.etFromDate.setOnClickListener {
            showDatePicker(binding.etFromDate)
        }

        // Set click listener for To Date
        binding.etToDate.setOnClickListener {
            showDatePicker(binding.etToDate)
        }

        binding.btnCreateTaskPage.setOnClickListener {
            archiveTask()
        }

        binding.btnResetFilters.setOnClickListener {
            binding.etFromDate.text?.clear()
            binding.etToDate.text?.clear()
            binding.seekBarMinDuration.progress = 0
            binding.seekBarMaxDuration.progress = 24
            applyFilters()
        }

        // Toggle Filters Button
        binding.btnToggleFilters.setOnClickListener {
            val isFilterOff = binding.btnToggleFilters.text == getString(R.string.filter_by_category_off)
            if (isFilterOff) {
                // Turn on filters
                binding.filterContainer.visibility = View.VISIBLE
                binding.btnToggleFilters.text = getString(R.string.filter_by_category_on)
                // Apply filters
                applyFilters()
            } else {
                // Turn off filters
                binding.filterContainer.visibility = View.GONE
                binding.btnToggleFilters.text = getString(R.string.filter_by_category_off)
                // Show all sessions
                showAllSessions()
            }
        }



        //Change btn based on isArchived Value
        if (isArchived) {
            binding.btnCreateTaskPage.text = "UN-ARCHIVE TASK"
            binding.btnCreateTaskPage.setOnClickListener {
                unarchiveTask()
            }
        } else {
            binding.btnCreateTaskPage.text = "ARCHIVE TASK"
            binding.btnCreateTaskPage.setOnClickListener {
                archiveTask()
            }
        }

    }

    // <editor-fold desc="Function for display UI">

    private fun populateUI(task: TaskItem) {
        // Populate the UI elements with task data

        binding.tvTaskTitle.text = task.title

        // Category chips
        binding.chipGroupCategory.removeAllViews() // Clear existing chips
        val categories = task.category.split(",")
        categories.forEach { category ->
            val chip = createChip(category.trim())
            binding.chipGroupCategory.addView(chip)
        }

        // Task date
        binding.tvTaskDate.setText(task.startDate)

        // Task time
        val timeNumber = task.time.substringAfter("Time: ").trim()
        binding.tvTaskTime.setText(timeNumber)

        // Min and Max Target Hours
        binding.tvMinHours.setText(task.minTargetHours.toString())
        binding.tvMaxHours.setText(task.maxTargetHours.toString())

        // Load sessions
        sessionAdapter.submitList(task.sessionHistory)
    }

    // Helper function to create a Chip for the category
    private fun createChip(category: String?): Chip {
        val chip = Chip(context)
        chip.text = category
        return chip
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
                    val dateStr = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    when (view.id) {
                        R.id.etFromDate -> {
                            binding.etFromDate.setText(dateStr)
                        }
                        R.id.etToDate -> {
                            binding.etToDate.setText(dateStr)
                        }
                        R.id.tvTaskDate -> {
                            binding.tvTaskDate.text = dateStr
                        }
                        // Add more cases if needed
                    }
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    // </editor-fold>

    // check if task was deleted or updated
    private val editTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val taskDeleted = result.data?.getBooleanExtra("taskDeleted", false) ?: false
            if (taskDeleted) {
                // Task was deleted, pop this fragment
                parentFragmentManager.popBackStack()
            } else {
                // Task was updated, refresh the UI
                taskId?.let { id ->
                    taskViewModel.getTaskById(id) { task ->
                        if (task != null) {
                            currentTask = task
                            populateUI(task)
                        }
                    }
                }
            }
        }
    }

    private fun openEditTaskActivity() {
        val intent = Intent(requireContext(), EditTaskActivity::class.java)
        intent.putExtra("taskId", taskId)
        editTaskLauncher.launch(intent)
    }

    // <editor-fold desc="Archive/Un-Archive">

    private fun archiveTask() {
        currentTask?.let { task ->
            task.isArchived = true
            taskViewModel.updateTask(task) {
                // Notify the user
                Toast.makeText(requireContext(), "Task archived", Toast.LENGTH_SHORT).show()
                // Close TaskInfoFragment
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun unarchiveTask() {
        currentTask?.let { task ->
            task.isArchived = false
            taskViewModel.updateTask(task) {
                Toast.makeText(requireContext(), "Task unarchived", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    // </editor-fold>

    private fun setupRecyclerView() {
        sessionAdapter = TaskSessionAdapter()
        binding.rvTaskHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sessionAdapter
        }
    }

    private val dateFilterWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            applyFilters()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun applyFilters() {

        if (binding.btnToggleFilters.text == getString(R.string.filter_by_category_off)) {
            // Filters are off, show all sessions
            showAllSessions()
            return
        }

        val fromDateStr = binding.etFromDate.text.toString()
        val toDateStr = binding.etToDate.text.toString()
        val minDuration = binding.seekBarMinDuration.progress
        val maxDuration = binding.seekBarMaxDuration.progress

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val filteredSessions = currentTask?.sessionHistory?.filter { session ->
            val sessionDate = dateFormat.parse(session.sessionStartDate)
            val fromDate = if (fromDateStr.isNotEmpty()) dateFormat.parse(fromDateStr) else null
            val toDate = if (toDateStr.isNotEmpty()) dateFormat.parse(toDateStr) else null

            val afterFrom = fromDate?.let { sessionDate >= it } ?: true
            val beforeTo = toDate?.let { sessionDate <= it } ?: true

            // Duration condition
            val sessionDurationHours = session.sessionDuration?.split(":")?.get(0)?.toIntOrNull() ?: 0
            val meetsDuration = sessionDurationHours in minDuration..maxDuration

            afterFrom && beforeTo && meetsDuration
        } ?: emptyList()

        sessionAdapter.submitList(filteredSessions)
    }

    private val durationFilterListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            applyFilters()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun showAllSessions() {
        sessionAdapter.submitList(currentTask?.sessionHistory ?: emptyList())
    }

}