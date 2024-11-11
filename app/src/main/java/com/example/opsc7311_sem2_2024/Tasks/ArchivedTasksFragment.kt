package com.example.opsc7311_sem2_2024.Tasks

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskAdapter
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.example.opsc7311_sem2_2024.databinding.FragmentArchivedTasksBinding
import com.google.android.material.chip.Chip

class ArchivedTasksFragment : Fragment(), TaskAdapter.TaskActionListener {

    private var _binding: FragmentArchivedTasksBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager()

    private lateinit var archivedAdapter: TaskAdapter

    private val archivedTasks = mutableListOf<TaskItem>()
    private val allCategories = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArchivedTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        loadArchivedTasksFromFirebase()

        binding.btnGoToTasks.setOnClickListener {
            // Open TasksFragment
            val tasksFragment = TasksFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, tasksFragment)
                .addToBackStack(null)
                .commit()
        }

        // Toggle Button Click Listener
        binding.btnToggleCategoryFilter.setOnClickListener {
            val isFilterOff = binding.btnToggleCategoryFilter.text == getString(R.string.filter_by_category_off)
            if (isFilterOff) {
                // Turn on the filter
                binding.categoryFilterContainer.visibility = View.VISIBLE
                binding.btnToggleCategoryFilter.text = getString(R.string.filter_by_category_on)
                // Apply category filter if any chips are selected
                applyCategoryFilter()
            } else {
                // Turn off the filter
                binding.categoryFilterContainer.visibility = View.GONE
                binding.btnToggleCategoryFilter.text = getString(R.string.filter_by_category_off)
                // Show all archived tasks
                showAllArchivedTasks()
            }
        }
    }

    private fun setupRecyclerView() {
        archivedAdapter = TaskAdapter(this)
        binding.rvArchivedTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = archivedAdapter
        }
    }

    private fun loadArchivedTasksFromFirebase() {
        firebaseManager.fetchTasks { tasks ->
            val tasksFromDb = tasks.filter { it.isArchived }

            archivedTasks.clear()
            allCategories.clear()

            for (task in tasksFromDb) {
                archivedTasks.add(task)
                val categories = task.category.split(",").map { it.trim() }
                allCategories.addAll(categories)
            }

            activity?.runOnUiThread {
                populateCategoryChips()
                showAllArchivedTasks()
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

        // If no categories are selected, show all archived tasks
        if (selectedCategories.isEmpty()) {
            showAllArchivedTasks()
            return
        }

        // Filter archived tasks based on selected categories
        val filteredArchivedTasks = archivedTasks.filter { task ->
            taskMatchesSelectedCategories(task, selectedCategories)
        }

        // Update RecyclerView
        archivedAdapter.submitList(filteredArchivedTasks)
    }

    private fun taskMatchesSelectedCategories(task: TaskItem, selectedCategories: List<String>): Boolean {
        val taskCategories = task.category.split(",").map { it.trim() }
        return taskCategories.any { it in selectedCategories }
    }

    private fun showAllArchivedTasks() {
        archivedAdapter.submitList(ArrayList(archivedTasks))
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
        Toast.makeText(requireContext(), "Un-Archive Task First", Toast.LENGTH_LONG).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
