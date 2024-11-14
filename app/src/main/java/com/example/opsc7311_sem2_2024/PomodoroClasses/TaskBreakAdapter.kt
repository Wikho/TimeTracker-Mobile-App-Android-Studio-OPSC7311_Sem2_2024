package com.example.opsc7311_sem2_2024.BreakInfo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem

class TaskBreakAdapter(
    var taskList: List<TaskItem>,
    private val onItemClick: (TaskItem) -> Unit
) : RecyclerView.Adapter<TaskBreakAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivTaskImage: ImageView = itemView.findViewById(R.id.ivTaskImage)
        val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        val tvTaskCategories: TextView = itemView.findViewById(R.id.tvTaskCategories)

        init {
            itemView.setOnClickListener {
                onItemClick(taskList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_break, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvTaskName.text = task.title
        holder.tvTaskCategories.text = task.category

        // Load image if available
        holder.ivTaskImage.setImageResource(R.drawable.ic_launcher_background)
    }

    override fun getItemCount(): Int = taskList.size

    fun updateList(newList: List<TaskItem>) {
        taskList = newList
        notifyDataSetChanged()
    }
}
