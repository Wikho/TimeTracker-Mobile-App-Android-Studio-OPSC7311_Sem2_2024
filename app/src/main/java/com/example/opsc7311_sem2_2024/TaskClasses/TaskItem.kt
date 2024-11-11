package com.example.opsc7311_sem2_2024.TaskClasses

import com.google.firebase.database.IgnoreExtraProperties

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
        var totalMinutes = 0
        for (session in sessionHistory) {
            session.sessionDuration?.let {
                val parts = it.split(":")
                if (parts.size >= 2) {
                    val hours = parts[0].toIntOrNull() ?: 0
                    val minutes = parts[1].toIntOrNull() ?: 0
                    totalMinutes += hours * 60 + minutes
                }
            }
        }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format("%02d:%02d", hours, minutes)
    }

    fun getTotalWorkedMinutes(): Int {
        var totalMinutes = 0
        for (session in sessionHistory) {
            session.sessionDuration?.let {
                val parts = it.split(":")
                if (parts.size >= 2) {
                    val hours = parts[0].toIntOrNull() ?: 0
                    val minutes = parts[1].toIntOrNull() ?: 0
                    totalMinutes += hours * 60 + minutes
                }
            }
        }
        return totalMinutes
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
