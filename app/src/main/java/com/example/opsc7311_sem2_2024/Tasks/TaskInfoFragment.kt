package com.example.opsc7311_sem2_2024.Tasks

import android.app.Activity
import androidx.activity.result.ActivityResult
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.example.opsc7311_sem2_2024.TaskClasses.TaskSessionAdapter
import com.example.opsc7311_sem2_2024.databinding.FragmentTaskInfoBinding
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class TaskInfoFragment : Fragment() {

    // Binding
    private var _binding: FragmentTaskInfoBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager()

    private var taskId: String? = null
    private var currentTask: TaskItem? = null
    private var isArchived: Boolean = false

    private lateinit var sessionAdapter: TaskSessionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = arguments?.getString("taskId")
        isArchived = arguments?.getBoolean("isArchived", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Fetch the task data in onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Fetch task data
        taskId?.let { id ->
            firebaseManager.fetchTasks { tasks ->
                currentTask = tasks.find { it.id == id }
                currentTask?.let { task ->
                    populateUI(task)
                } ?: run {
                    Toast.makeText(requireContext(), "Task not found", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
        }

        // Recycler view to Show Session History
        setupRecyclerView()

        // Click listener for the back button
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Click listener for the edit button
        binding.btnEditTask.setOnClickListener {
            openEditTaskActivity()
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
            if (isArchived) {
                unarchiveTask()
            } else {
                archiveTask()
            }
        }

        binding.btnResetFilters.setOnClickListener {
            binding.etFromDate.text?.clear()
            binding.etToDate.text?.clear()
            binding.seekBarMinDuration.progress = 0
            binding.seekBarMaxDuration.progress = 9
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

        // Change btn based on isArchived Value
        if (isArchived) {
            binding.btnCreateTaskPage.text = "UN-ARCHIVE TASK"
        } else {
            binding.btnCreateTaskPage.text = "ARCHIVE TASK"
        }

        // Add TextWatchers for filters
        binding.etFromDate.addTextChangedListener(dateFilterWatcher)
        binding.etToDate.addTextChangedListener(dateFilterWatcher)
        binding.seekBarMinDuration.setOnSeekBarChangeListener(durationFilterListener)
        binding.seekBarMaxDuration.setOnSeekBarChangeListener(durationFilterListener)
    }

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
        binding.tvTaskDate.text = task.startDate

        // Task time
        val timeNumber = task.time.substringAfter("Time: ").trim()
        binding.tvTaskTime.text = timeNumber

        // Min and Max Target Hours
        binding.tvMinHours.text = task.minTargetHours.toString()
        binding.tvMaxHours.text = task.maxTargetHours.toString()

        // Load sessions
        sessionAdapter.submitList(task.sessionHistory)
    }

    private fun createChip(category: String?): Chip {
        val chip = Chip(context)
        chip.text = category
        return chip
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        activity?.let {
            val datePickerDialog = DatePickerDialog(
                it,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dateStr = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    editText.setText(dateStr)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private val editTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val taskDeleted = result.data?.getBooleanExtra("taskDeleted", false) ?: false
            if (taskDeleted) {
                // Task was deleted, pop this fragment
                parentFragmentManager.popBackStack()
            } else {
                // Task was updated, refresh the UI
                taskId?.let { id ->
                    firebaseManager.fetchTasks { tasks ->
                        currentTask = tasks.find { it.id == id }
                        currentTask?.let { task ->
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

    private fun archiveTask() {
        currentTask?.let { task ->
            task.isArchived = true
            firebaseManager.updateTask(task) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "Task archived", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun unarchiveTask() {
        currentTask?.let { task ->
            task.isArchived = false
            firebaseManager.updateTask(task) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "Task unarchived", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
