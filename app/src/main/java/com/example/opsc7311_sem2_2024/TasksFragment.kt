package com.example.opsc7311_sem2_2024

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.databinding.FragmentTasksBinding
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TasksFragment : Fragment(), TaskAdapter.TaskActionListener {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

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
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()

        // Load tasks from the database in a coroutine
        lifecycleScope.launch {
            loadTasksFromDatabase()
        }

        // Listen for 'taskCreated' fragment result to refresh the RecyclerView
        parentFragmentManager.setFragmentResultListener("taskCreated", viewLifecycleOwner) { key, bundle ->
            // Reload tasks from database
            lifecycleScope.launch {
                loadTasksFromDatabase()
            }
        }

        binding.fabAddTask.setOnClickListener {
            // Create an instance of TaskCreationFragment
            val taskCreationFragment = TaskCreationFragment()

            // Get the FragmentManager and begin a transaction
            val transaction = parentFragmentManager.beginTransaction()

            // Use add or replace to overlay the fragment
            transaction.add(R.id.fragment_container, taskCreationFragment)

            // Add the transaction to the back stack so the user can navigate back
            transaction.addToBackStack(null)

            // Commit the transaction
            transaction.commit()
        }

        binding.btnToggleCategoryFilter.setOnClickListener {
            val isFilterOff = binding.btnToggleCategoryFilter.text == getString(R.string.filter_by_category_off)
            if (isFilterOff) {
                // Turn on the filter
                binding.categoryFilterContainer.visibility = View.VISIBLE
                binding.btnToggleCategoryFilter.text = getString(R.string.filter_by_category_on)
                // Apply category filter if any chips are selected
                //applyCategoryFilter()
            } else {
                // Turn off the filter
                binding.categoryFilterContainer.visibility = View.GONE
                binding.btnToggleCategoryFilter.text = getString(R.string.filter_by_category_off)
                // Show all tasks
                //showAllTasks()
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

    private suspend fun loadTasksFromDatabase() {
        // Fetch tasks from the database
        val taskDatabase = TaskDatabase.getDatabase(requireContext().applicationContext)
        val taskDao = taskDatabase.taskItemDao()
        val tasksFromDb = taskDao.getAllTasks()

        // Clear previous data
        allTasks.clear()
        todayTasks.clear()
        thisWeekTasks.clear()
        upcomingTasks.clear()
        allCategories.clear()

        val currentDate = getCurrentDate()
        val currentWeekRange = getCurrentWeekRange()

        for (task in tasksFromDb) {
            if (task.isArchived) continue  // Skip archived tasks

            allTasks.add(task)

            // Collect categories
            val categories = task.category.split(",").map { it.trim() }
            allCategories.addAll(categories)

            val taskDate = task.startDate

            when {
                taskDate == currentDate -> todayTasks.add(task)
                taskDate in currentWeekRange -> thisWeekTasks.add(task)
                taskDate > currentWeekRange.last() -> upcomingTasks.add(task)
            }
        }

        // Update RecyclerViews with tasks
        withContext(Dispatchers.Main) {
            // Populate category chips
            populateCategoryChips()

            // Initially show all tasks
            showAllTasks()
        }
    }

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
        todayAdapter.submitList(ArrayList(todayTasks))  // Create new lists to prevent issues
        thisWeekAdapter.submitList(ArrayList(thisWeekTasks))
        upcomingAdapter.submitList(ArrayList(upcomingTasks))
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
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val startOfWeek = calendar.time
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

    override fun onStopButtonClicked(task: TaskItem) {
        stopSession(task)
    }

    private fun showSessionDialog(task: TaskItem) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_task_description, null)
        val etSessionDescription = dialogView.findViewById<TextInputEditText>(R.id.etSessionDescription)
        val btnSelectPhoto = dialogView.findViewById<Button>(R.id.btnSelectPhoto)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelSession)
        val btnDone = dialogView.findViewById<Button>(R.id.btnDoneSession)

        // Set the dialog title with the task name
        tvDialogTitle.text = "${task.title} Description for Next Session"

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

        // Handle Done button
        btnDone.setOnClickListener {
            val description = etSessionDescription.text.toString()
            startSession(task, description, selectedImagePath)
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun startSession(task: TaskItem, description: String, imagePath: String?) {
        val currentTime = System.currentTimeMillis()
        val sessionStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(currentTime))
        val startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentTime))

        val newSession = TaskSession(
            sessionDescription = description,
            sessionStartDate = sessionStartDate,
            startTime = startTime,
            imagePath = imagePath ?: "default_image_path" // Use default if null
        )

        // Update the task
        task.sessionHistory.add(newSession)
        task.isStarted = true

        // Update the database
        lifecycleScope.launch {
            val taskDatabase = TaskDatabase.getDatabase(requireContext().applicationContext)
            val taskDao = taskDatabase.taskItemDao()
            taskDao.updateTask(task)

            // Refresh the list
            loadTasksFromDatabase()
        }
    }

    private fun stopSession(task: TaskItem) {
        val currentTime = System.currentTimeMillis()
        val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentTime))

        // Find the active session
        val activeSession = task.sessionHistory.lastOrNull { it.endTime == null }
        if (activeSession != null) {
            activeSession.endTime = endTime

            // Calculate session duration
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startDate = format.parse(activeSession.startTime)
            val endDate = format.parse(endTime)
            val difference = endDate.time - startDate.time
            val minutes = (difference / (1000 * 60)).toInt()
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            activeSession.sessionDuration = String.format("%d:%02d", hours, remainingMinutes)
        }

        task.isStarted = false

        // Update the database
        lifecycleScope.launch {
            val taskDatabase = TaskDatabase.getDatabase(requireContext().applicationContext)
            val taskDao = taskDatabase.taskItemDao()
            taskDao.updateTask(task)

            // Refresh the list
            loadTasksFromDatabase()
        }

        // Update category stats
        // Update the database
        lifecycleScope.launch {
            val taskDatabase = TaskDatabase.getDatabase(requireContext().applicationContext)
            val taskDao = taskDatabase.taskItemDao()
            taskDao.updateTask(task)

            // Refresh the list
            loadTasksFromDatabase()
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

    private fun updateCategoryStats(task: TaskItem, session: TaskSession) {
        val categories = task.category.split(",").map { it.trim() }
        val sessionDurationMinutes = calculateSessionDurationInMinutes(session.sessionDuration)

        lifecycleScope.launch {
            val taskDatabase = TaskDatabase.getDatabase(requireContext().applicationContext)
            val taskDao = taskDatabase.taskItemDao()

            for (category in categories) {
                var categoryStats = taskDao.getCategoryStats(category)
                if (categoryStats == null) {
                    categoryStats = CategoryStats(categoryName = category, totalMinutes = sessionDurationMinutes)
                } else {
                    categoryStats.totalMinutes += sessionDurationMinutes
                }
                taskDao.insertOrUpdateCategoryStats(categoryStats)
            }
        }
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


    // </editor-fold>

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
