package com.example.opsc7311_sem2_2024.Notes

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.example.opsc7311_sem2_2024.databinding.DeleteConfirmationBinding
import com.example.opsc7311_sem2_2024.databinding.DialogAddNoteBinding
import com.example.opsc7311_sem2_2024.databinding.FragmentNotesBinding
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

// <editor-fold desc="Mode Enum">
enum class NoteMode {
    TODO,
    IMPORTANCE,
    DONE,
    ALL
}
// </editor-fold>

class NotesFragment : Fragment(), NotesAdapter.NoteItemListener {

    // <editor-fold desc="Variables and Initialization">
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var notesAdapter: NotesAdapter
    private val notesList = mutableListOf<Note>()
    private val displayedNotesList = mutableListOf<Note>()
    private var currentMode: NoteMode = NoteMode.TODO

    private val firebaseManager = FirebaseManager()

    private val allTasksList = mutableListOf<TaskItem>()
    private val filteredTasksList = mutableListOf<TaskItem>()
    private val allCategoriesSet = mutableSetOf<String>()
    private var selectedTask: TaskItem? = null

    private var isSessionMode: Boolean = false
    private var taskId: String? = null
    // </editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isSessionMode = it.getBoolean("isSessionMode", false)
            taskId = it.getString("taskId")
        }
    }

    // <editor-fold desc="Lifecycle Methods">
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // </editor-fold>

    // <editor-fold desc="onViewCreated">
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        notesAdapter = NotesAdapter(displayedNotesList, this)
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = notesAdapter


        if (isSessionMode) {
            // Hide llFilterAndSpinner
            binding.llFilterAndSpinner.visibility = View.GONE
            // Show tvTaskName and back button
            binding.tvTaskName.visibility = View.VISIBLE
            binding.btnBack.visibility = View.VISIBLE

            // Set task name
            taskId?.let { id ->
                firebaseManager.getTaskById(id) { taskItem ->
                    activity?.runOnUiThread {
                        binding.tvTaskName.text = taskItem?.title ?: "Task"
                    }
                }
            }

            // Load notes for the task
            taskId?.let { id ->
                firebaseManager.getTaskById(id) { taskItem ->
                    selectedTask = taskItem
                    if (selectedTask != null) {
                        loadNotesForTask(selectedTask!!)
                    }
                }
            }

            // Set back button listener
            binding.btnBack.setOnClickListener {
                requireActivity().onBackPressed()
            }
        } else {
            // Normal mode
            binding.llFilterAndSpinner.visibility = View.VISIBLE
            binding.tvTaskName.visibility = View.GONE
            binding.btnBack.visibility = View.GONE

            // Toggle Filter Button
            binding.btnToggleFilter.setOnClickListener {
                val isFilterOff = binding.btnToggleFilter.text == "Filter Off"
                if (isFilterOff) {
                    binding.filterContainer.visibility = View.VISIBLE
                    binding.btnToggleFilter.text = "Filter On"
                } else {
                    binding.filterContainer.visibility = View.GONE
                    binding.btnToggleFilter.text = "Filter Off"
                    binding.etSearchTask.text?.clear()
                    binding.etFromDate.text?.clear()
                    binding.etToDate.text?.clear()
                    binding.chipGroupCategories.clearCheck()
                    loadTasks() // Reload tasks without filters
                }
            }

            // Date Pickers
            binding.etFromDate.setOnClickListener { showDatePicker(binding.etFromDate) }
            binding.etToDate.setOnClickListener { showDatePicker(binding.etToDate) }

            // Text Change Listener for Search
            binding.etSearchTask.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Call filter function when text changes
                    applyFilters()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // No action needed here
                }
                override fun afterTextChanged(s: Editable?) {
                    // No action needed here
                }
            })

            // Setup Category Chips
            setupCategoryChips()

            // Load tasks
            loadTasks()

        }

        // Mode Button Setup
        binding.btnMode.setOnClickListener {
            showModeSelectionDialog()
        }

        // Add New Note Button
        binding.btnAddNewNote.setOnClickListener {
            showAddNoteDialog()
        }
    }
    // </editor-fold>

    // <editor-fold desc="Date Picker">
    private fun showDatePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                editText.setText(dateStr)
                applyFilters()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    // </editor-fold>

    // <editor-fold desc="Category Chips">
    private fun setupCategoryChips() {
        firebaseManager.fetchCategories { categories ->
            activity?.runOnUiThread {
                binding.chipGroupCategories.removeAllViews()
                for (category in categories) {
                    val chip = Chip(requireContext()).apply {
                        text = category
                        isCheckable = true
                        setOnCheckedChangeListener { _, _ ->
                            applyFilters()
                        }
                    }
                    binding.chipGroupCategories.addView(chip)
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Load and Filter Tasks">
    private fun loadTasks() {
        firebaseManager.fetchTasks { tasks ->
            activity?.runOnUiThread {
                allTasksList.clear()
                allTasksList.addAll(tasks)
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val searchText = binding.etSearchTask.text.toString().trim().lowercase()
        val fromDateStr = binding.etFromDate.text.toString()
        val toDateStr = binding.etToDate.text.toString()
        val selectedCategories = binding.chipGroupCategories.checkedChipIds.map { id ->
            binding.chipGroupCategories.findViewById<Chip>(id).text.toString()
        }

        filteredTasksList.clear()
        for (task in allTasksList) {
            var matches = true

            // Filter by search text
            if (searchText.isNotEmpty() && !task.title.lowercase().contains(searchText)) {
                matches = false
            }

            // Filter by date range
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val taskDate = dateFormat.parse(task.startDate)
            if (fromDateStr.isNotEmpty()) {
                val fromDate = dateFormat.parse(fromDateStr)
                if (taskDate.before(fromDate)) {
                    matches = false
                }
            }
            if (toDateStr.isNotEmpty()) {
                val toDate = dateFormat.parse(toDateStr)
                if (taskDate.after(toDate)) {
                    matches = false
                }
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

        // Update the spinner
        updateTaskSpinner()
    }

    private fun updateTaskSpinner() {
        val taskTitles = filteredTasksList.map { it.title }
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            taskTitles
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSelectTask.adapter = spinnerAdapter

        binding.spinnerSelectTask.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedTask = filteredTasksList[position]
                loadNotesForTask(selectedTask!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedTask = null
                notesList.clear()
                notesAdapter.notifyDataSetChanged()
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Load Notes for Task">
    private fun loadNotesForTask(task: TaskItem) {
        firebaseManager.fetchNotes(task.id) { notes ->
            activity?.runOnUiThread {
                notesList.clear()
                notesList.addAll(notes)
                filterNotesByMode()
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Filter Notes by Mode">
    private fun filterNotesByMode() {
        displayedNotesList.clear()
        when (currentMode) {
            NoteMode.TODO -> {
                displayedNotesList.addAll(notesList.filter { !it.isCompleted })
            }
            NoteMode.DONE -> {
                displayedNotesList.addAll(notesList.filter { it.isCompleted })
            }
            NoteMode.ALL -> {
                displayedNotesList.addAll(notesList)
            }
            NoteMode.IMPORTANCE -> {
                displayedNotesList.addAll(notesList)
                // Sort by importance level
                displayedNotesList.sortWith(compareByDescending<Note> { getImportanceLevel(it.importance) }.thenBy { it.title })
            }
        }
        notesAdapter.notifyDataSetChanged()
    }

    private fun getImportanceLevel(importance: String): Int {
        return when (importance) {
            "High" -> 3
            "Medium" -> 2
            "Low" -> 1
            else -> 0
        }
    }
    // </editor-fold>

    // <editor-fold desc="Mode Selection Dialog">
    private fun showModeSelectionDialog() {
        val modes = arrayOf("To Do", "Importance", "Done", "All")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Mode")
        builder.setItems(modes) { dialog, which ->
            when (which) {
                0 -> {
                    currentMode = NoteMode.TODO
                    binding.btnMode.text = "Mode: To Do"
                }
                1 -> {
                    currentMode = NoteMode.IMPORTANCE
                    binding.btnMode.text = "Mode: Importance"
                }
                2 -> {
                    currentMode = NoteMode.DONE
                    binding.btnMode.text = "Mode: Done"
                }
                3 -> {
                    currentMode = NoteMode.ALL
                    binding.btnMode.text = "Mode: All"
                }
            }
            filterNotesByMode()
        }
        builder.show()
    }
    // </editor-fold>

    // <editor-fold desc="Add/Edit Note Dialog">
    private fun showAddNoteDialog(noteToEdit: Note? = null) {
        val dialogBinding = DialogAddNoteBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        if (noteToEdit != null) {
            dialogBinding.etNoteTitle.setText(noteToEdit.title)
            dialogBinding.etNoteDescription.setText(noteToEdit.description)
            when (noteToEdit.importance) {
                "High" -> dialogBinding.rgImportance.check(R.id.rbHigh)
                "Medium" -> dialogBinding.rgImportance.check(R.id.rbMedium)
                "Low" -> dialogBinding.rgImportance.check(R.id.rbLow)
            }
            dialogBinding.btnCreateNote.text = "Save"
        }
        else {
            // Set default importance based on setting
            val defaultPriority = SettingsSingleton.getSettingValue("Note Default Priority") as? String ?: "Medium"
            when (defaultPriority) {
                "High" -> dialogBinding.rgImportance.check(R.id.rbHigh)
                "Medium" -> dialogBinding.rgImportance.check(R.id.rbMedium)
                "Low" -> dialogBinding.rgImportance.check(R.id.rbLow)
            }
        }

        dialogBinding.btnCancelNote.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnCreateNote.setOnClickListener {
            val title = dialogBinding.etNoteTitle.text.toString().trim()
            val description = dialogBinding.etNoteDescription.text.toString().trim()

            val selectedImportanceId = dialogBinding.rgImportance.checkedRadioButtonId
            val importance = when (selectedImportanceId) {
                R.id.rbHigh -> "High"
                R.id.rbMedium -> "Medium"
                R.id.rbLow -> "Low"
                else -> "Medium" // Default
            }

            if (title.isEmpty()) {
                dialogBinding.tilNoteTitle.error = "Title cannot be empty"
                return@setOnClickListener
            } else {
                dialogBinding.tilNoteTitle.error = null
            }

            val noteId = noteToEdit?.id ?: UUID.randomUUID().toString()
            val newNote = Note(
                id = noteId,
                title = title,
                description = description,
                isCompleted = noteToEdit?.isCompleted ?: false,
                importance = importance
            )

            // Save note to Firebase
            selectedTask?.let { task ->
                firebaseManager.saveNote(task.id, newNote) { success, message ->
                    if (success) {
                        if (noteToEdit != null) {
                            val index = notesList.indexOfFirst { it.id == noteToEdit.id }
                            if (index != -1) {
                                notesList[index] = newNote
                            }
                        } else {
                            notesList.add(newNote)
                        }
                        filterNotesByMode()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }
    // </editor-fold>

    // <editor-fold desc="NoteItemListener Implementation">
    override fun onNoteChecked(note: Note, isChecked: Boolean) {
        note.isCompleted = isChecked
        selectedTask?.let { task ->
            firebaseManager.saveNote(task.id, note) { success, message ->
                if (success) {
                    val index = notesList.indexOfFirst { it.id == note.id }
                    if (index != -1) {
                        notesList[index] = note
                        filterNotesByMode()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onNoteLongPressed(note: Note) {
        showNoteOptionsDialog(note)
    }

    private fun showNoteOptionsDialog(note: Note) {
        val options = arrayOf("Edit", "Delete", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Action")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Edit
                    showAddNoteDialog(note)
                }
                1 -> {
                    // Delete
                    showDeleteNoteConfirmation(note)
                }
                2 -> {
                    // Cancel
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }
    // </editor-fold>

    // <editor-fold desc="Delete Note Confirmation">
    private fun showDeleteNoteConfirmation(note: Note) {
        val builder = AlertDialog.Builder(requireContext())

        val dialogBinding = DeleteConfirmationBinding.inflate(LayoutInflater.from(requireContext()))

        builder.setView(dialogBinding.root)
        val alertDialog = builder.create()

        dialogBinding.tvDeleteConfirmation.text = "Are you sure you want to delete the note?"

        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.btnDelete.setOnClickListener {
            // Delete note from Firebase
            selectedTask?.let { task ->
                firebaseManager.deleteNote(task.id, note.id) { success, message ->
                    if (success) {
                        notesList.remove(note)
                        filterNotesByMode()
                        alertDialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        alertDialog.show()
    }
    // </editor-fold>
}
