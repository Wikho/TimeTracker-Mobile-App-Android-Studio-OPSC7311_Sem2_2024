package com.example.opsc7311_sem2_2024.BreakInfo

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskSession

class TaskSessionBreakAdapter(
    var sessionList: List<TaskSession>,
    private val onItemClick: (TaskSession) -> Unit
) : RecyclerView.Adapter<TaskSessionBreakAdapter.SessionViewHolder>() {

    private val firebaseManager = FirebaseManager()

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivSessionImage: ImageView = itemView.findViewById(R.id.ivSessionImage)
        val tvSessionDate: TextView = itemView.findViewById(R.id.tvSessionDate)
        val tvSessionDescription: TextView = itemView.findViewById(R.id.tvSessionDescription)
        val tvTotalBreaks: TextView = itemView.findViewById(R.id.tvTotalBreaks)
        val tvTotalBreakDuration: TextView = itemView.findViewById(R.id.tvTotalBreakDuration)

        init {
            itemView.setOnClickListener {
                onItemClick(sessionList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_session_break, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessionList[position]
        holder.tvSessionDate.text = session.sessionStartDate
        holder.tvSessionDescription.text = session.sessionDescription

        // Load image if available
        if (!session.imagePath.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(session.imagePath)
                .placeholder(R.drawable.image)
                .into(holder.ivSessionImage)
        } else {
            holder.ivSessionImage.setImageResource(R.drawable.image)
        }

        // Fetch breaks for this session
        if (!session.taskId.isNullOrEmpty() && !session.sessionId.isNullOrEmpty()) {
            firebaseManager.fetchBreaksForSession(session.taskId!!, session.sessionId!!) { breaks ->
                val totalBreaks = breaks.size
                val totalBreakDurationInSeconds = breaks.sumOf { pomodoroBreak ->
                    pomodoroBreak.duration?.let { calculateDurationInSeconds(it) } ?: 0L
                }
                val totalBreakDurationFormatted = formatDuration(totalBreakDurationInSeconds)
                holder.tvTotalBreaks.text = "Total Breaks: $totalBreaks"
                holder.tvTotalBreakDuration.text = "Total Break Duration: $totalBreakDurationFormatted"
            }
        } else {
            holder.tvTotalBreaks.text = "Total Breaks: 0"
            holder.tvTotalBreakDuration.text = "Total Break Duration: 00:00:00"
        }
    }

    override fun getItemCount(): Int = sessionList.size

    fun updateList(newList: List<TaskSession>) {
        sessionList = newList
        notifyDataSetChanged()
    }

    private fun calculateDurationInSeconds(duration: String): Long {
        return try {
            val parts = duration.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toLongOrNull() ?: 0L
                val minutes = parts[1].toLongOrNull() ?: 0L
                val seconds = parts[2].toLongOrNull() ?: 0L
                hours * 3600 + minutes * 60 + seconds
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e("TaskSessionBreakAdapter", "Error calculating duration: ${e.message}")
            0L
        }
    }

    private fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }
}
