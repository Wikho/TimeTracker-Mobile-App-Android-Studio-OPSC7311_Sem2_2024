package com.example.opsc7311_sem2_2024.BreakInfo

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import java.util.*

class BreakInfoFragment : Fragment() {

    private lateinit var btnBack: Button
    private lateinit var btnFilterToggle: Button
    private lateinit var btnTask: Button
    private lateinit var btnNoTask: Button
    private lateinit var btnPrevious: Button
    private lateinit var rvBreakInfo: RecyclerView
    private lateinit var filterContainer: LinearLayout
    private lateinit var etSearch: EditText
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText

    private val firebaseManager = FirebaseManager()

    private var currentViewState: ViewState = ViewState.TASKS
    private var selectedTaskId: String? = null
    private var selectedSessionId: String? = null

    enum class ViewState {
        TASKS,
        SESSIONS,
        BREAKS
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_break_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initialize UI components
        btnBack = view.findViewById(R.id.btnBack)
        btnFilterToggle = view.findViewById(R.id.btnFilterToggle)
        btnTask = view.findViewById(R.id.btnTask)
        btnNoTask = view.findViewById(R.id.btnNoTask)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        rvBreakInfo = view.findViewById(R.id.rvBreakInfo)
        filterContainer = view.findViewById(R.id.filterContainer)
        etSearch = view.findViewById(R.id.etSearch)
        etFromDate = view.findViewById(R.id.etFromDate)
        etToDate = view.findViewById(R.id.etToDate)

        // Set up RecyclerView
        rvBreakInfo.layoutManager = LinearLayoutManager(requireContext())

        // Set up buttons
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnFilterToggle.setOnClickListener {
            toggleFilter()
        }

        btnTask.setOnClickListener {
            currentViewState = ViewState.TASKS
            btnPrevious.visibility = View.GONE
            loadTasks()
        }

        btnNoTask.setOnClickListener {
            currentViewState = ViewState.BREAKS
            btnPrevious.visibility = View.GONE
            loadUnspecifiedBreaks()
        }

        btnPrevious.setOnClickListener {
            when (currentViewState) {
                ViewState.SESSIONS -> {
                    currentViewState = ViewState.TASKS
                    btnPrevious.visibility = View.GONE
                    loadTasks()
                }
                ViewState.BREAKS -> {
                    currentViewState = ViewState.SESSIONS
                    loadSessionsForTask(selectedTaskId!!)
                }
                else -> {
                    // Do nothing
                }
            }
        }

        // Handle arguments if any
        val args = arguments
        val taskIdFromArgs = args?.getString("taskId")
        val sessionIdFromArgs = args?.getString("sessionId")

        if (taskIdFromArgs != null && sessionIdFromArgs != null) {
            // We have taskId and sessionId, load breaks for session
            selectedTaskId = taskIdFromArgs
            selectedSessionId = sessionIdFromArgs
            currentViewState = ViewState.BREAKS
            btnPrevious.visibility = View.VISIBLE
            loadBreaksForSession(selectedTaskId!!, selectedSessionId!!)
        } else {
            // Load initial data
            btnTask.performClick()
        }

        // Set date pickers
        etFromDate.setOnClickListener {
            showDatePicker { date ->
                etFromDate.setText(date)
                applyFilters()
            }
        }

        etToDate.setOnClickListener {
            showDatePicker { date ->
                etToDate.setText(date)
                applyFilters()
            }
        }

        // Add TextWatcher for search
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
    }

    private fun toggleFilter() {
        val isFilterOff = btnFilterToggle.text == "Filter Off"
        if (isFilterOff) {
            filterContainer.visibility = View.VISIBLE
            btnFilterToggle.text = "Filter On"
        } else {
            filterContainer.visibility = View.GONE
            btnFilterToggle.text = "Filter Off"
            etSearch.text.clear()
            etFromDate.text.clear()
            etToDate.text.clear()
            applyFilters()
        }
    }

    private fun loadTasks() {
        firebaseManager.fetchTasks { tasks ->
            val adapter = TaskBreakAdapter(tasks) { task ->
                selectedTaskId = task.id
                currentViewState = ViewState.SESSIONS
                btnPrevious.visibility = View.VISIBLE
                loadSessionsForTask(task.id)
            }
            rvBreakInfo.adapter = adapter
            applyFilters()
        }
    }

    private fun loadSessionsForTask(taskId: String) {
        firebaseManager.getTaskById(taskId) { task ->
            task?.let {
                val adapter = TaskSessionBreakAdapter(it.sessionHistory) { session ->
                    selectedSessionId = session.sessionId
                    currentViewState = ViewState.BREAKS
                    btnPrevious.visibility = View.VISIBLE
                    loadBreaksForSession(taskId, session.sessionId)
                }
                rvBreakInfo.adapter = adapter
                applyFilters()
            } ?: run {
                // No sessions
                rvBreakInfo.adapter = null
            }
        }
    }

    private fun loadBreaksForSession(taskId: String, sessionId: String) {
        firebaseManager.fetchBreaksForSession(taskId, sessionId) { breaks ->
            if (breaks.isNotEmpty()) {
                val adapter = BreakAdapter(breaks)
                rvBreakInfo.adapter = adapter
                applyFilters()
            } else {
                // Show a message if no breaks are found
                rvBreakInfo.adapter = null
                Toast.makeText(requireContext(), "No breaks found for this session.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUnspecifiedBreaks() {
        firebaseManager.fetchUnspecifiedBreaks { breaks ->
            val adapter = BreakAdapter(breaks)
            rvBreakInfo.adapter = adapter
            applyFilters()
        }
    }

    private fun applyFilters() {
        val searchText = etSearch.text.toString().trim().lowercase()
        val fromDateStr = etFromDate.text.toString()
        val toDateStr = etToDate.text.toString()

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val fromDate = if (fromDateStr.isNotEmpty()) dateFormat.parse(fromDateStr) else null
        val toDate = if (toDateStr.isNotEmpty()) dateFormat.parse(toDateStr) else null

        when (val adapter = rvBreakInfo.adapter) {
            is TaskBreakAdapter -> {
                val filteredTasks = adapter.taskList.filter { task ->
                    val matchesSearch = searchText.isEmpty() || task.title.lowercase().contains(searchText)
                    val taskDate = dateFormat.parse(task.startDate)
                    val afterFrom = fromDate?.let { taskDate >= it } ?: true
                    val beforeTo = toDate?.let { taskDate <= it } ?: true
                    matchesSearch && afterFrom && beforeTo
                }
                adapter.updateList(filteredTasks)
            }
            is TaskSessionBreakAdapter -> {
                val filteredSessions = adapter.sessionList.filter { session ->
                    val matchesSearch = searchText.isEmpty() || session.sessionDescription.lowercase().contains(searchText)
                    val sessionDate = dateFormat.parse(session.sessionStartDate)
                    val afterFrom = fromDate?.let { sessionDate >= it } ?: true
                    val beforeTo = toDate?.let { sessionDate <= it } ?: true
                    matchesSearch && afterFrom && beforeTo
                }
                adapter.updateList(filteredSessions)
            }
            is BreakAdapter -> {
                val filteredBreaks = adapter.breakList.filter { pomodoroBreak ->
                    val matchesSearch = searchText.isEmpty() || pomodoroBreak.reason.lowercase().contains(searchText)
                    val breakDate = dateFormat.parse(pomodoroBreak.date)
                    val afterFrom = fromDate?.let { breakDate >= it } ?: true
                    val beforeTo = toDate?.let { breakDate <= it } ?: true
                    matchesSearch && afterFrom && beforeTo
                }
                adapter.updateList(filteredBreaks)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val dateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(dateStr)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}
