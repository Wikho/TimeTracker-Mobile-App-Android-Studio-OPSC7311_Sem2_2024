package com.example.opsc7311_sem2_2024.TaskClasses

import com.google.firebase.database.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@IgnoreExtraProperties
data class TaskItem(
    var id: String = "",
    var title: String = "",
    var category: String = "",
    var startDate: String = "",
    var time: String = "",
    var minTargetHours: Int = 0,
    var maxTargetHours: Int = 0,
    var isArchived: Boolean = false,
    var isStarted: Boolean = false,
    var sessionHistory: MutableList<TaskSession> = mutableListOf()
) {
    fun getTotalSessionDuration(): String {
        var totalSeconds = 0L
        for (session in sessionHistory) {
            val duration = session.sessionDuration ?: continue
            val parts = duration.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                val seconds = parts[2].toIntOrNull() ?: 0
                totalSeconds += hours * 3600 + minutes * 60 + seconds
            }
        }
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun getTotalWorkedMinutesToday(): Int {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var totalMinutes = 0
        for (session in sessionHistory) {
            if (session.sessionStartDate == todayDate) {
                session.sessionDuration?.let {
                    val parts = it.split(":")
                    if (parts.size >= 2) {
                        val hours = parts[0].toIntOrNull() ?: 0
                        val minutes = parts[1].toIntOrNull() ?: 0
                        totalMinutes += hours * 60 + minutes
                    }
                }
            }
        }
        return totalMinutes
    }

    fun getTotalSessionDurationInMinutes(): Int {
        var totalMinutes = 0
        for (session in sessionHistory) {
            totalMinutes += calculateDurationInMinutes(session.sessionDuration)
        }
        return totalMinutes
    }

    // Helper function
    private fun calculateDurationInMinutes(duration: String?): Int {
        duration?.let {
            val parts = it.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                val seconds = parts[2].toIntOrNull() ?: 0
                return hours * 60 + minutes + if (seconds >= 30) 1 else 0 // Round up if 30 seconds or more
            }
        }
        return 0
    }
}

// Data class for each session of a task
@IgnoreExtraProperties
data class TaskSession(
    var sessionId: String = "",
    var sessionDescription: String = "",
    var sessionStartDate: String = "",
    var startTime: String = "",
    var endTime: String? = null,
    var sessionDuration: String? = null,
    var imagePath: String? = null,
    var breakCount: Int = 0,
    var totalBreakTime: Long = 0L
)
