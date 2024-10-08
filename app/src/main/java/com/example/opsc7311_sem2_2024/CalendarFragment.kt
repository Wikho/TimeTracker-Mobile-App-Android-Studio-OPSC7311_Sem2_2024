package com.example.opsc7311_sem2_2024

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.databinding.FragmentCalendarBinding
import com.google.android.material.chip.Chip
import com.prolificinteractive.materialcalendarview.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarFragment : Fragment(), TaskAdapter.TaskActionListener {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter
    private val allTasks = mutableListOf<TaskItem>()
    private val allCategories = mutableSetOf<String>()
    private var isFilterOn = false

    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize TaskAdapter
        taskAdapter = TaskAdapter(this)
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter

        // Load tasks from database
        loadTasksFromDatabase()

        // Set up category filter
        setupCategoryFilter()

        // Set up calendar
        setupCalendarView()
    }

    private fun loadTasksFromDatabase() {
        val taskDatabase = TaskDatabase.getDatabase(requireContext())
        val taskDao = taskDatabase.taskItemDao()
        lifecycleScope.launch {
            val tasks = taskDao.getAllTasks()
            allTasks.clear()
            allTasks.addAll(tasks)

            // Collect all categories
            for (task in allTasks) {
                val categories = task.category.split(",").map { it.trim() }
                allCategories.addAll(categories)
            }

            withContext(Dispatchers.Main) {
                // Populate category chips
                populateCategoryChips()

                // Update calendar with event dots
                addEventDotsToCalendar()

                // Display tasks for selected date
                displayTasksForSelectedDate()
            }
        }
    }

    private fun setupCategoryFilter() {
        binding.btnToggleCategoryFilter.setOnClickListener {
            isFilterOn = !isFilterOn
            if (isFilterOn) {
                binding.btnToggleCategoryFilter.text = "Filter by Category: ON"
                binding.categoryFilterContainer.visibility = View.VISIBLE
                applyCategoryFilter()
            } else {
                binding.btnToggleCategoryFilter.text = "Filter by Category: OFF"
                binding.categoryFilterContainer.visibility = View.GONE
                displayTasksForSelectedDate()
            }
        }
    }

    private fun populateCategoryChips() {
        binding.chipGroupCategoryFilter.removeAllViews()
        for (category in allCategories) {
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                setOnCheckedChangeListener { _, _ ->
                    if (isFilterOn) {
                        applyCategoryFilter()
                    }
                }
            }
            binding.chipGroupCategoryFilter.addView(chip)
        }
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            selectedDate = LocalDate.of(date.year, date.month + 1, date.day)
            if (isFilterOn) {
                applyCategoryFilter()
            } else {
                displayTasksForSelectedDate()
            }
        }

        // Set current date
        binding.calendarView.selectedDate = CalendarDay.today()
    }

    private fun displayTasksForSelectedDate() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val selectedDateString = selectedDate.format(formatter)

        val tasksForDate = allTasks.filter { task ->
            task.startDate == selectedDateString
        }

        taskAdapter.submitList(tasksForDate)
    }

    private fun applyCategoryFilter() {
        val selectedCategories = binding.chipGroupCategoryFilter.checkedChipIds.map { id ->
            val chip = binding.chipGroupCategoryFilter.findViewById<Chip>(id)
            chip.text.toString()
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val selectedDateString = selectedDate.format(formatter)

        val filteredTasks = allTasks.filter { task ->
            val taskCategories = task.category.split(",").map { it.trim() }
            task.startDate == selectedDateString && taskCategories.any { it in selectedCategories }
        }

        taskAdapter.submitList(filteredTasks)
    }

    private fun addEventDotsToCalendar() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val eventDates = allTasks.mapNotNull { task ->
            try {
                val date = LocalDate.parse(task.startDate, formatter)
                CalendarDay.from(date.year, date.monthValue - 1, date.dayOfMonth)
            } catch (e: Exception) {
                null
            }
        }.toSet()

        val decorator = EventDecorator(Color.RED, eventDates)
        binding.calendarView.addDecorator(decorator)
    }

    override fun onTaskLongPressed(task: TaskItem) {
        // Handle task long press
    }

    override fun onStartButtonClicked(task: TaskItem) {
        // Handle start button click
    }

}
