package com.example.opsc7311_sem2_2024

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.databinding.FragmentNotesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesFragment : Fragment(), TaskNoteAdapter.NoteActionListener {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskNoteAdapter: TaskNoteAdapter
    private val tasksList = mutableListOf<TaskItem>()
    private var selectedTask: TaskItem? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        taskNoteAdapter = TaskNoteAdapter(this)
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = taskNoteAdapter

        // Load tasks and set up spinner
        loadTasks()

        // Set up Add Note button
        binding.btnAddNote.setOnClickListener {
            addNewNote()
        }
    }

    private fun loadTasks() {
        val taskDatabase = TaskDatabase.getDatabase(requireContext())
        val taskDao = taskDatabase.taskItemDao()

        lifecycleScope.launch {
            val tasks = taskDao.getAllTasks()
            tasksList.clear()
            tasksList.addAll(tasks)

            withContext(Dispatchers.Main) {
                val taskTitles = tasksList.map { it.title }
                val spinnerAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    taskTitles
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerSelectTask.adapter = spinnerAdapter

                binding.spinnerSelectTask.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View, position: Int, id: Long
                    ) {
                        selectedTask = tasksList[position]
                        displayNotesForSelectedTask()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        selectedTask = null
                        taskNoteAdapter.submitList(emptyList())
                    }
                }
            }
        }
    }

    private fun displayNotesForSelectedTask() {
        selectedTask?.let { task ->
           // taskNoteAdapter.submitList(ArrayList(task.notes))
        }
    }

    private fun addNewNote() {
        val noteContent = binding.etNewNote.text.toString().trim()
        if (TextUtils.isEmpty(noteContent)) {
            binding.tilNewNote.error = "Note cannot be empty"
            return
        } else {
            binding.tilNewNote.error = null
        }

        selectedTask?.let { task ->
            val newNote = TaskNote(
                content = noteContent,
                isCompleted = false
            )
            //task.notes.add(newNote)

            // Update task in the database
            val taskDatabase = TaskDatabase.getDatabase(requireContext())
            val taskDao = taskDatabase.taskItemDao()
            lifecycleScope.launch {
                taskDao.updateTask(task)
                withContext(Dispatchers.Main) {
                    binding.etNewNote.text?.clear()
                    displayNotesForSelectedTask()
                    Toast.makeText(requireContext(), "Note added", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(requireContext(), "Please select a task", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNoteChecked(note: TaskNote, isChecked: Boolean) {
        note.isCompleted = isChecked
        selectedTask?.let { task ->
            // Update task in the database
            val taskDatabase = TaskDatabase.getDatabase(requireContext())
            val taskDao = taskDatabase.taskItemDao()
            lifecycleScope.launch {
                taskDao.updateTask(task)
            }
        }
    }

    override fun onNoteDeleted(note: TaskNote) {
        selectedTask?.let { task ->
            //task.notes.remove(note)
            // Update task in the database
            val taskDatabase = TaskDatabase.getDatabase(requireContext())
            val taskDao = taskDatabase.taskItemDao()
            lifecycleScope.launch {
                taskDao.updateTask(task)
                withContext(Dispatchers.Main) {
                    displayNotesForSelectedTask()
                    Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
