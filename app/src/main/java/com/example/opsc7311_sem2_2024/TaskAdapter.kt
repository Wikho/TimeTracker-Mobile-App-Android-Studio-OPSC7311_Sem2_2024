package com.example.opsc7311_sem2_2024

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.databinding.TaskItemLayoutBinding

class TaskAdapter(private val listener: TaskActionListener) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val taskList = mutableListOf<TaskItem>()


    fun submitList(tasks: List<TaskItem>) {
        taskList.clear()
        taskList.addAll(tasks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        // Use the correct binding class: TaskItemLayoutBinding
        val binding = TaskItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)


    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    inner class TaskViewHolder(private val binding: TaskItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskItem) {
            // Bind data to the views
            binding.tvTaskTileTitle.text = task.title
            binding.tvTaskTileTag.text = task.category

            // Set button states
            binding.btnTaskTileStart.isEnabled = !task.isStarted
            binding.btnTaskTileStop.isEnabled = task.isStarted

            // Extract the task time and session duration
            val taskTime = task.time
            val totalSessionDuration = task.getTotalSessionDuration()

            binding.tvTaskTileTime.text = "Time: $taskTime / $totalSessionDuration"

            // Handle long press
            binding.root.setOnLongClickListener {
                listener.onTaskLongPressed(task)
                true // Indicate that the long press event was handled
            }

            // Handle Start button click
            binding.btnTaskTileStart.setOnClickListener {
                listener.onStartButtonClicked(task)
            }

            // Handle Stop button click
            binding.btnTaskTileStop.setOnClickListener {
                listener.onStopButtonClicked(task)
            }

        }
    }

    interface TaskActionListener {
        fun onTaskLongPressed(task: TaskItem)
        fun onStartButtonClicked(task: TaskItem)
        fun onStopButtonClicked(task: TaskItem)

    }

}
