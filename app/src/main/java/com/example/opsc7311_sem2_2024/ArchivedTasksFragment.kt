package com.example.opsc7311_sem2_2024

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.databinding.FragmentArchivedTasksBinding
import kotlinx.coroutines.launch


class ArchivedTasksFragment : Fragment(), TaskAdapter.TaskActionListener {

    private var _binding: FragmentArchivedTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var archivedAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchivedTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Load archived tasks
        lifecycleScope.launch {
            loadArchivedTasksFromDatabase()
        }

        binding.btnGoToTasks.setOnClickListener {
            // Open ArchiveTasksFragment
            val TasksFragment = TasksFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TasksFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupRecyclerView() {
        archivedAdapter = TaskAdapter(this)
        binding.rvArchivedTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = archivedAdapter
        }
    }

    private suspend fun loadArchivedTasksFromDatabase() {
        val taskDatabase = TaskDatabase.getDatabase(requireContext().applicationContext)
        val taskDao = taskDatabase.taskItemDao()
        val archivedTasks = taskDao.getArchivedTasks()
        archivedAdapter.submitList(archivedTasks)
    }

    override fun onTaskLongPressed(task: TaskItem) {
        // Open TaskInfoFragment for the archived task
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
        Toast.makeText(requireContext(),"Un-Archive Task First",Toast.LENGTH_LONG).show()
    }

    override fun onStopButtonClicked(task: TaskItem) {
        Toast.makeText(requireContext(),"Un-Archive Task First",Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}