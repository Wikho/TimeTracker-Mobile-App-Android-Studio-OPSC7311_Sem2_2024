package com.example.opsc7311_sem2_2024

import TaskManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.databinding.FragmentTasksBinding

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

        // Initialize the taskAdapter
        todayAdapter = TaskAdapter(mutableListOf(), this)
        thisWeekAdapter = TaskAdapter(mutableListOf(), this)
        upcomingAdapter = TaskAdapter(mutableListOf(), this)

        // Set up RecyclerView for rvToday
        binding.rvToday.layoutManager = LinearLayoutManager(context)
        binding.rvToday.adapter = todayAdapter

        // Set up RecyclerView for rvThisWeek
        binding.rvThisWeek.layoutManager = LinearLayoutManager(context)
        binding.rvThisWeek.adapter = thisWeekAdapter

        // Set up RecyclerView for Upcoming
        binding.rvUpcoming.layoutManager = LinearLayoutManager(context)
        binding.rvUpcoming.adapter = upcomingAdapter

        // Add some tasks programmatically
        addTestTasks()

        return binding.root
    }

    private fun addTestTasks() {
        val task1 = TaskItem(
            title = "Test Task 1",
            category = "Category 1",
            time = "10:00 AM",
            creationDate = "2024-01-01",
            creationTime = "09:00 AM",
            startDate = "2024-01-01",
            minTargetHours = 2,
            maxTargetHours = 5,
            isStarted = false
        )


        upcomingAdapter.addTask(task1)
        upcomingAdapter.addTask(task1)
        upcomingAdapter.addTask(task1)

        thisWeekAdapter.addTask(task1)
        thisWeekAdapter.addTask(task1)

        todayAdapter.addTask(task1)

        //Add Button FAB
        binding.fabAddTask.setOnClickListener {
            // Handle the FAB click
            // You can launch a new activity or dialog to create a new task
        }

    }



    // Implement TaskActionListener methods
    override fun onStartClick(task: TaskItem) {
        // Handle start click logic
    }

    override fun onStopClick(task: TaskItem) {
        // Handle stop click logic
    }

    override fun onArchiveClick(task: TaskItem) {
        // Handle archive click logic
    }

    override fun onImageClick(task: TaskItem) {
        // Handle image click logic
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
