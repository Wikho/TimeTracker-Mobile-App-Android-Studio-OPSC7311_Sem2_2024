package com.example.opsc7311_sem2_2024

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.databinding.TaskItemLayoutBinding

class TaskAdapter(
    private val taskList: MutableList<TaskItem>,
    private val listener: TaskActionListener
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // Define the TaskActionListener interface
    interface TaskActionListener {
        fun onStartClick(task: TaskItem)
        fun onStopClick(task: TaskItem)
        fun onArchiveClick(task: TaskItem)
        fun onImageClick(task: TaskItem)
    }

    class TaskViewHolder(val binding: TaskItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        // Bind task data to the views in task_item_layout.xml
        holder.binding.tvTaskTileTitle.text = task.title
        holder.binding.tvTaskTileTag.text = task.category
        holder.binding.tvTaskTileTime.text = task.time

        // Set button click listeners
        holder.binding.btnTaskTileStart.setOnClickListener { listener.onStartClick(task) }
        holder.binding.btnTaskTileStop.setOnClickListener { listener.onStopClick(task) }
        holder.binding.fabBtnTaskTileArchive.setOnClickListener { listener.onArchiveClick(task) }
        holder.binding.fabBtnTaskTileImage.setOnClickListener { listener.onImageClick(task) }

        // Enable/Disable the stop button based on whether the task is started
        holder.binding.btnTaskTileStop.isEnabled = task.isStarted
    }

    override fun getItemCount(): Int = taskList.size

    // Function to add multiple tasks
    fun addTasks(tasks: List<TaskItem>) {
        taskList.addAll(tasks)
        notifyDataSetChanged()
    }

    // Function to add a single task
    fun addTask(task: TaskItem) {
        taskList.add(task)
        notifyItemInserted(taskList.size - 1)
    }
}