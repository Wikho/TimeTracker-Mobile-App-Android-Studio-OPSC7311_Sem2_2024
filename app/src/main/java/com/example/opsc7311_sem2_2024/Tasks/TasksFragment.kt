package com.example.opsc7311_sem2_2024.Tasks

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.Session.SessionStartedActivity
import com.example.opsc7311_sem2_2024.TaskClasses.TaskAdapter
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.example.opsc7311_sem2_2024.TaskClasses.TaskSession
import com.example.opsc7311_sem2_2024.databinding.FragmentTasksBinding
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class TasksFragment : Fragment(), TaskAdapter.TaskActionListener {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager()

    private lateinit var todayAdapter: TaskAdapter
    private lateinit var thisWeekAdapter: TaskAdapter
    private lateinit var upcomingAdapter: TaskAdapter

    private val allTasks = mutableListOf<TaskItem>()
    private val todayTasks = mutableListOf<TaskItem>()
    private val thisWeekTasks = mutableListOf<TaskItem>()
    private val upcomingTasks = mutableListOf<TaskItem>()
    private val allCategories = mutableSetOf<String>()

    private var selectedImagePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerViews()
        loadTasksFromFirebase()

        // Listen for 'taskCreated' fragment result to refresh the RecyclerView
        parentFragmentManager.setFragmentResultListener("taskCreated", viewLifecycleOwner) { _, _ ->
            // Reload tasks from Firebase
            loadTasksFromFirebase()
        }

        binding.fabAddTask.setOnClickListener {
            // Open TaskCreationFragment
            val taskCreationFragment = TaskCreationFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, taskCreationFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnToggleCategoryFilter.setOnClickListener {
            val isFilterOff = binding.btnToggleCategoryFilter.text == getString(R.string.filter_by_category_off)
            if (isFilterOff) {
                // Turn on the filter
                binding.categoryFilterContainer.visibility = View.VISIBLE
                binding.btnToggleCategoryFilter.text = getString(R.string.filter_by_category_on)
                // Apply category filter if any chips are selected
                applyCategoryFilter()
            } else {
                // Turn off the filter
                binding.categoryFilterContainer.visibility = View.GONE
                binding.btnToggleCategoryFilter.text = getString(R.string.filter_by_category_off)
                // Show all tasks
                showAllTasks()
            }
        }

        binding.btnArchivedTasks.setOnClickListener {
            // Open ArchiveTasksFragment
            val archiveTasksFragment = ArchivedTasksFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, archiveTasksFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    // <editor-fold desc="RecyclerViews Setup">

    private fun setupRecyclerViews() {
        // Setup RecyclerViews for today, this week, and upcoming
        todayAdapter = TaskAdapter(this)
        binding.rvToday.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todayAdapter
        }

        thisWeekAdapter = TaskAdapter(this)
        binding.rvThisWeek.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = thisWeekAdapter
        }

        upcomingAdapter = TaskAdapter(this)
        binding.rvUpcoming.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = upcomingAdapter
        }
    }

    private fun loadTasksFromFirebase() {
        firebaseManager.fetchTasks { tasks ->
            if (tasks != null && tasks.isNotEmpty()) {
                val tasksToArchive = mutableListOf<TaskItem>()
                val today = getStartOfToday()

                tasks.forEach { task ->
                    try {
                        val taskDateStr = task.startDate
                        if (!taskDateStr.isNullOrEmpty()) {
                            val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(taskDateStr)
                            if (taskDate != null && taskDate.before(today) && !task.isArchived) {
                                task.isArchived = true
                                tasksToArchive.add(task)
                            }
                        } else {
                            // Handle tasks with null or empty startDate
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Handle parsing exceptions
                    }
                }

                // Update archived tasks in Firebase
                if (tasksToArchive.isNotEmpty()) {
                    tasksToArchive.forEach { taskToArchive ->
                        firebaseManager.updateTask(taskToArchive) { success, message ->
                            if (!success) {
                                activity?.runOnUiThread {
                                    Toast.makeText(requireContext(), "Error archiving task: $message", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                activity?.runOnUiThread {
                    processTasks(tasks.filter { !it.isArchived })
                }
            } else {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "No tasks found", Toast.LENGTH_SHORT).show()
                    // Clear previous data
                    allTasks.clear()
                    todayTasks.clear()
                    thisWeekTasks.clear()
                    upcomingTasks.clear()
                    allCategories.clear()

                    // Update UI
                    populateCategoryChips()
                    showAllTasks()
                }
            }
        }
    }

    private fun processTasks(tasks: List<TaskItem>) {
        // Clear previous data
        allTasks.clear()
        todayTasks.clear()
        thisWeekTasks.clear()
        upcomingTasks.clear()
        allCategories.clear()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val startOfToday = getStartOfToday()
        val startOfWeek = getStartOfWeek()
        val endOfWeek = getEndOfWeek()

        tasks.forEach { task ->
            if (task.isArchived) return@forEach  // Skip archived tasks

            allTasks.add(task)

            // Collect categories
            val categories = task.category?.split(",")?.map { it.trim() } ?: emptyList()
            allCategories.addAll(categories)

            val taskDateStr = task.startDate
            if (!taskDateStr.isNullOrEmpty()) {
                try {
                    val taskDate = dateFormat.parse(taskDateStr)
                    if (taskDate != null) {
                        when {
                            isSameDay(taskDate, startOfToday) -> todayTasks.add(task)
                            isDateBetween(taskDate, startOfToday, endOfWeek) -> thisWeekTasks.add(task)
                            taskDate.after(endOfWeek) -> upcomingTasks.add(task)
                            else -> {
                                // Task is before today
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle parsing exceptions
                }
            }
        }

        // Update RecyclerViews with tasks on the main thread
        activity?.runOnUiThread {
            // Populate category chips
            populateCategoryChips()

            // Initially show all tasks
            showAllTasks()
        }
    }


    // <editor-fold desc="Helper function for RecyclerViews Setup">

    private fun getStartOfToday(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun getStartOfWeek(): Date {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        // Set to the first day of the week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        // Reset time to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun getEndOfWeek(): Date {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        // Set to the first day of the week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        // Move to Sunday
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        // Set time to end of day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isDateBetween(date: Date, startDate: Date, endDate: Date): Boolean {
        return (date.after(startDate) || isSameDay(date, startDate)) &&
                (date.before(endDate) || isSameDay(date, endDate))
    }


    // </editor-fold>

    private fun populateCategoryChips() {
        binding.chipGroupCategoryFilter.removeAllViews()
        for (category in allCategories) {
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                setOnCheckedChangeListener { _, _ ->
                    if (binding.btnToggleCategoryFilter.text == getString(R.string.filter_by_category_on)) {
                        applyCategoryFilter()
                    }
                }
            }
            binding.chipGroupCategoryFilter.addView(chip)
        }
    }

    private fun applyCategoryFilter() {
        val selectedCategories = binding.chipGroupCategoryFilter.checkedChipIds.map { id ->
            val chip = binding.chipGroupCategoryFilter.findViewById<Chip>(id)
            chip.text.toString()
        }

        // If no categories are selected, show all tasks
        if (selectedCategories.isEmpty()) {
            showAllTasks()
            return
        }

        // Filter tasks based on selected categories
        val filteredTodayTasks = todayTasks.filter { task ->
            taskMatchesSelectedCategories(task, selectedCategories)
        }
        val filteredThisWeekTasks = thisWeekTasks.filter { task ->
            taskMatchesSelectedCategories(task, selectedCategories)
        }
        val filteredUpcomingTasks = upcomingTasks.filter { task ->
            taskMatchesSelectedCategories(task, selectedCategories)
        }

        // Update RecyclerViews
        todayAdapter.submitList(filteredTodayTasks)
        thisWeekAdapter.submitList(filteredThisWeekTasks)
        upcomingAdapter.submitList(filteredUpcomingTasks)
    }

    private fun taskMatchesSelectedCategories(task: TaskItem, selectedCategories: List<String>): Boolean {
        val taskCategories = task.category.split(",").map { it.trim() }
        return taskCategories.any { it in selectedCategories }
    }

    private fun showAllTasks() {
        todayAdapter.submitList(ArrayList(todayTasks))
        thisWeekAdapter.submitList(ArrayList(thisWeekTasks))
        upcomingAdapter.submitList(ArrayList(upcomingTasks))

        // Set visibility of sections based on whether they have tasks
        binding.tvToday.visibility = if (todayTasks.isEmpty()) View.GONE else View.VISIBLE
        binding.rvToday.visibility = if (todayTasks.isEmpty()) View.GONE else View.VISIBLE

        binding.tvThisWeek.visibility = if (thisWeekTasks.isEmpty()) View.GONE else View.VISIBLE
        binding.rvThisWeek.visibility = if (thisWeekTasks.isEmpty()) View.GONE else View.VISIBLE

        binding.tvUpcoming.visibility = if (upcomingTasks.isEmpty()) View.GONE else View.VISIBLE
        binding.rvUpcoming.visibility = if (upcomingTasks.isEmpty()) View.GONE else View.VISIBLE
    }

    // </editor-fold>

    // <editor-fold desc="Task Press & Functions">

    // Get today's date in "yyyy-MM-dd" format
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Get the date range for the current week
    private fun getCurrentWeekRange(): List<String> {
        val calendar = Calendar.getInstance()
        // Adjust the calendar to the start of the week (Sunday or Monday based on locale)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val weekRange = mutableListOf<String>()

        for (i in 0..6) {
            weekRange.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weekRange
    }

    override fun onTaskLongPressed(task: TaskItem) {
        // Create a bundle to pass task ID
        val bundle = Bundle().apply {
            putString("taskId", task.id) // Pass the task ID
        }

        // Create the TaskInfoFragment and pass the task ID
        val taskInfoFragment = TaskInfoFragment().apply {
            arguments = bundle
        }

        // Replace the current fragment with TaskInfoFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, taskInfoFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onStartButtonClicked(task: TaskItem) {
        showSessionDialog(task)
    }

    private fun showSessionDialog(task: TaskItem) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_task_description, null)
        val etSessionDescription = dialogView.findViewById<EditText>(R.id.etSessionDescription)
        val btnSelectPhoto = dialogView.findViewById<Button>(R.id.btnSelectPhoto)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelSession)
        val btnStart = dialogView.findViewById<Button>(R.id.btnStartSession)

        // Set the dialog title with the task name
        tvDialogTitle.text = "Start ${task.title} session"

        // Handle Select Photo button
        btnSelectPhoto.setOnClickListener {
            selectImageFromGallery()
        }

        // Create the AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Handle Cancel button
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Handle Start button
        btnStart.setOnClickListener {
            val description = etSessionDescription.text.toString().trim()
            if (description.isEmpty()) {
                etSessionDescription.error = "Description cannot be empty"
                return@setOnClickListener
            }
            startSession(task, description, selectedImagePath)
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun startSession(task: TaskItem, description: String, imagePath: String?) {
        val currentTime = System.currentTimeMillis()
        val sessionStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(currentTime))
        val startTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))

        val newSession = TaskSession(
            sessionId = UUID.randomUUID().toString(),
            sessionDescription = description,
            sessionStartDate = sessionStartDate,
            startTime = startTime,
            imagePath = imagePath
        )

        // Update the task
        task.sessionHistory.add(newSession)
        task.isStarted = true

        // Update the task in Firebase
        firebaseManager.updateTask(task) { success, message ->
            if (success) {
                // Open SessionStartedActivity
                val intent = Intent(requireContext(), SessionStartedActivity::class.java)
                intent.putExtra("taskId", task.id)
                intent.putExtra("sessionId", newSession.sessionId)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImagePath = it.toString()
        }
    }

    private fun selectImageFromGallery() {
        selectImageLauncher.launch("image/*")
    }



    // </editor-fold>

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
