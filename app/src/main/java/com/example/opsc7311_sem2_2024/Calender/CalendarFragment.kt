package com.example.opsc7311_sem2_2024.Calendar

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskAdapter
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.example.opsc7311_sem2_2024.Tasks.TaskCreationFragment
import com.example.opsc7311_sem2_2024.Tasks.TaskInfoFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment(), TaskAdapter.TaskActionListener {

    // UI Components
    private lateinit var btnToggleFilter: Button
    private lateinit var filterContainer: LinearLayout
    private lateinit var etSearchTask: EditText
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var btnToggleCalendar: Button
    private lateinit var calendarView: CalendarView
    private lateinit var rvTasks: RecyclerView
    private lateinit var btnCreateTask: Button

    // Variables
    private val firebaseManager = FirebaseManager()
    private val allTasksList = mutableListOf<TaskItem>()
    private val filteredTasksList = mutableListOf<TaskItem>()
    private lateinit var taskAdapter: TaskAdapter
    private val allCategoriesSet = mutableSetOf<String>()

    private var selectedDate: String = ""
    private var selectedDateDisplay: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Initialize UI components
        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)
        filterContainer = view.findViewById(R.id.filterContainer)
        etSearchTask = view.findViewById(R.id.etSearchTask)
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories)
        btnToggleCalendar = view.findViewById(R.id.btnToggleCalendar)
        calendarView = view.findViewById(R.id.calendarView)
        rvTasks = view.findViewById(R.id.rvTasks)
        btnCreateTask = view.findViewById(R.id.btnCreateTask)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Initialize Task Adapter
        taskAdapter = TaskAdapter(this)
        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        rvTasks.adapter = taskAdapter

        // Set default selected date to today
        val calendar = Calendar.getInstance()
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        selectedDateDisplay = SimpleDateFormat("MMM d", Locale.getDefault()).format(calendar.time)
        btnCreateTask.text = "Create Task for $selectedDateDisplay"

        // Load tasks and categories
        loadTasks()
        setupCategoryChips()

        // Listeners
        btnToggleFilter.setOnClickListener {
            toggleFilter()
        }

        btnToggleCalendar.setOnClickListener {
            toggleCalendar()
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            selectedDate = dateStr
            val displayDate = Calendar.getInstance()
            displayDate.set(year, month, dayOfMonth)
            selectedDateDisplay = SimpleDateFormat("MMM d", Locale.getDefault()).format(displayDate.time)
            btnCreateTask.text = "Create Task for $selectedDateDisplay"
            applyFilters()
        }

        etSearchTask.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        btnCreateTask.setOnClickListener {
            openTaskCreationFragment()
        }
    }

    private fun toggleFilter() {
        val isFilterOff = btnToggleFilter.text == "Filter Off"
        if (isFilterOff) {
            filterContainer.visibility = View.VISIBLE
            btnToggleFilter.text = "Filter On"
        } else {
            filterContainer.visibility = View.GONE
            btnToggleFilter.text = "Filter Off"
            etSearchTask.text.clear()
            chipGroupCategories.clearCheck()
            applyFilters()
        }
    }

    private fun toggleCalendar() {
        val isCalendarVisible = calendarView.visibility == View.VISIBLE
        if (isCalendarVisible) {
            calendarView.visibility = View.GONE
            btnToggleCalendar.text = "Show Calendar"
        } else {
            calendarView.visibility = View.VISIBLE
            btnToggleCalendar.text = "Hide Calendar"
        }
    }

    private fun loadTasks() {
        firebaseManager.fetchTasks { tasks ->
            activity?.runOnUiThread {
                allTasksList.clear()
                allTasksList.addAll(tasks)
                applyFilters()
                extractCategories()
            }
        }
    }

    private fun extractCategories() {
        allCategoriesSet.clear()
        for (task in allTasksList) {
            val categories = task.category.split(",").map { it.trim() }
            allCategoriesSet.addAll(categories)
        }
        setupCategoryChips()
    }

    private fun setupCategoryChips() {
        activity?.runOnUiThread {
            chipGroupCategories.removeAllViews()
            for (category in allCategoriesSet) {
                val chip = Chip(requireContext()).apply {
                    text = category
                    isCheckable = true
                    setOnCheckedChangeListener { _, _ ->
                        applyFilters()
                    }
                }
                chipGroupCategories.addView(chip)
            }
        }
    }

    private fun applyFilters() {
        val searchText = etSearchTask.text.toString().trim().lowercase()
        val selectedCategories = chipGroupCategories.checkedChipIds.map { id ->
            chipGroupCategories.findViewById<Chip>(id).text.toString()
        }

        filteredTasksList.clear()
        for (task in allTasksList) {
            var matches = true

            // Filter by selected date
            if (task.startDate != selectedDate) {
                matches = false
            }

            // Filter by search text
            if (searchText.isNotEmpty() && !task.title.lowercase().contains(searchText)) {
                matches = false
            }

            // Filter by categories
            if (selectedCategories.isNotEmpty()) {
                val taskCategories = task.category.split(",").map { it.trim() }
                if (!taskCategories.any { it in selectedCategories }) {
                    matches = false
                }
            }

            if (matches) {
                filteredTasksList.add(task)
            }
        }

        taskAdapter.submitList(filteredTasksList)
    }

    private fun openTaskCreationFragment() {
        val fragment = TaskCreationFragment()
        val bundle = Bundle()
        bundle.putString("selectedDate", selectedDate)
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("CalendarFragment")
            .commit()
    }

    // TaskAdapter.TaskActionListener implementation
    override fun onTaskLongPressed(task: TaskItem) {
        val bundle = Bundle().apply {
            putString("taskId", task.id)
            putBoolean("isArchived", true)
        }

        val taskInfoFragment = TaskInfoFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, taskInfoFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onStartButtonClicked(task: TaskItem) {
        // Show message that task can only be started in the Task panel
        Toast.makeText(requireContext(), "Task can only be started in the Task panel.", Toast.LENGTH_LONG).show()
    }
}
