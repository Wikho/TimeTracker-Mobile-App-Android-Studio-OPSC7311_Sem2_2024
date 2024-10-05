package com.example.opsc7311_sem2_2024

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.databinding.FragmentTasksBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TasksFragment : Fragment(), TaskAdapter.TaskActionListener {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var todayAdapter: TaskAdapter
    private lateinit var thisWeekAdapter: TaskAdapter
    private lateinit var upcomingAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)

        setupRecyclerViews()

        // Load tasks from the database in a coroutine
        lifecycleScope.launch {
            loadTasksFromDatabase()
        }

        binding.fabAddTask.setOnClickListener(){

            // Create an instance of TaskCreationFragment
            val taskCreationFragment = TaskCreationFragment()

            // Get the FragmentManager and begin a transaction
            val transaction = parentFragmentManager.beginTransaction()

            // Use add or replace to overlay the fragment
            transaction.add(R.id.fragment_container, taskCreationFragment) // or replace if you want to replace the current fragment

            // Add the transaction to the back stack so the user can navigate back
            transaction.addToBackStack(null)

            // Commit the transaction
            transaction.commit()

        }

        return binding.root
    }

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
        val allTasks = taskDao.getAllTasks()

        // Categorize tasks
        val todayTasks = mutableListOf<TaskItem>()
        val thisWeekTasks = mutableListOf<TaskItem>()
        val upcomingTasks = mutableListOf<TaskItem>()

        val currentDate = getCurrentDate()
        val currentWeekRange = getCurrentWeekRange()

        for (task in allTasks) {
            if (task.isArchived) continue  // Skip archived tasks

            val taskDate = task.startDate

            when {
                taskDate == currentDate -> todayTasks.add(task)
                taskDate in currentWeekRange -> thisWeekTasks.add(task)
                taskDate > currentWeekRange.last() -> upcomingTasks.add(task)
            }
        }

        // Update RecyclerViews with filtered tasks
        todayAdapter.submitList(todayTasks)
        thisWeekAdapter.submitList(thisWeekTasks)
        upcomingAdapter.submitList(upcomingTasks)
    }

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
        // Create a bundle to pass task details
        val bundle = Bundle().apply {
            putString("taskTitle", task.title)
            putString("taskCategory", task.category)
            putString("taskTime", task.time)
        }

        // Create the TaskInfoFragment and pass the task data
        val taskInfoFragment = TaskInfoFragment().apply {
            arguments = bundle
        }

        // Replace the current fragment with TaskInfoFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, taskInfoFragment) // Use your container ID
            .addToBackStack(null)
            .commit()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
