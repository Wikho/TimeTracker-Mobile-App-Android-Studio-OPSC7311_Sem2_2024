package com.example.opsc7311_sem2_2024.BreakInfo

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opsc7311_sem2_2024.Pomodoro.PomodoroBreak
import com.example.opsc7311_sem2_2024.R
import java.text.SimpleDateFormat
import java.util.Locale

class BreakAdapter(
    var breakList: List<PomodoroBreak>
) : RecyclerView.Adapter<BreakAdapter.BreakViewHolder>() {

    inner class BreakViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBreakImage: ImageView = itemView.findViewById(R.id.ivBreakImage)
        val tvBreakDate: TextView = itemView.findViewById(R.id.tvBreakDate)
        val tvBreakDescription: TextView = itemView.findViewById(R.id.tvBreakDescription)
        val tvBreakLength: TextView = itemView.findViewById(R.id.tvBreakLength)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreakViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_break, parent, false)
        return BreakViewHolder(view)
    }

    override fun onBindViewHolder(holder: BreakViewHolder, position: Int) {
        val pomodoroBreak = breakList[position]
        holder.tvBreakDate.text = pomodoroBreak.date
        holder.tvBreakDescription.text = pomodoroBreak.reason
        holder.tvBreakLength.text = if (!pomodoroBreak.duration.isNullOrEmpty()) {
            "Duration: ${pomodoroBreak.duration}"
        } else {
            if (!pomodoroBreak.endTime.isNullOrEmpty() && !pomodoroBreak.startTime.isNullOrEmpty()) {
                val calculatedDuration = calculateDuration(pomodoroBreak.startTime, pomodoroBreak.endTime!!)
                if (calculatedDuration.isNotEmpty()) {
                    "Duration: $calculatedDuration"
                } else {
                    "Duration: N/A"
                }
            } else {
                "Duration: N/A"
            }
        }

        // Load image if available
        if (!pomodoroBreak.imagePath.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(pomodoroBreak.imagePath)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivBreakImage)
        } else {
            holder.ivBreakImage.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    override fun getItemCount(): Int = breakList.size

    fun updateList(newList: List<PomodoroBreak>) {
        breakList = newList
        notifyDataSetChanged()
    }

    private fun calculateDuration(startTime: String, endTime: String): String {
        return try {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val startDate = format.parse(startTime)
            val endDate = format.parse(endTime)
            val difference = endDate.time - startDate.time
            val seconds = (difference / 1000) % 60
            val minutes = (difference / (1000 * 60)) % 60
            val hours = (difference / (1000 * 60 * 60))
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } catch (e: Exception) {
            Log.e("BreakAdapter", "Error calculating duration: ${e.message}")
            ""
        }
    }
}
