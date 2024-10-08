package com.example.opsc7311_sem2_2024

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.databinding.FragmentNotesBinding

class NotesFragment : Fragment(), NotesAdapter.NoteItemListener {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var notesAdapter: NotesAdapter
    private val notesList = mutableListOf<Note>()

    private val tasksList = mutableListOf<String>()
    private var selectedTask: String? = null

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
        notesAdapter = NotesAdapter(notesList, this)
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = notesAdapter

        // Load tasks and set up spinner
        loadTasks()

        // Set up Add Note button
        binding.btnSubmitNote.setOnClickListener {
            addNewNote()
        }
    }

    private fun loadTasks() {
        // Simulate loading tasks
        tasksList.addAll(listOf("Task 1", "Task 2", "Task 3"))

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tasksList
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSelectTask.adapter = spinnerAdapter

        binding.spinnerSelectTask.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedTask = tasksList[position]
                // For simplicity, clear notes when task changes
                notesList.clear()
                notesAdapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedTask = null
                notesList.clear()
                notesAdapter.notifyDataSetChanged()
            }
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

        val newNote = Note(
            content = noteContent,
            isCompleted = false
        )

        notesAdapter.addNote(newNote)
        binding.etNewNote.text?.clear()
        Toast.makeText(requireContext(), "Note added", Toast.LENGTH_SHORT).show()
    }

    override fun onNoteChecked(note: Note, isChecked: Boolean) {
        // Handle note checked/unchecked
        // No additional action needed for now
    }

    override fun onNoteDeleted(note: Note) {
        notesAdapter.removeNote(note)
        Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show()
    }
}
