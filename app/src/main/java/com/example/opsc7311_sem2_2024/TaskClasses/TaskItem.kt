package com.example.opsc7311_sem2_2024.TaskClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

//Sir this is the main and driving variables that we use for our Task asd Session i hope the comment help with each var use case

@Entity(tableName = "task_items")

// Data class for the task
data class TaskItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // Unique identifier for the task
    var title: String,                             // Title of the task
    var category: String,                          // Category associated with the task
    var time: String,                              // Time associated with the task
    val creationDate: String,                      // Date when the task was created
    val creationTime: String,                      // Time when the task was created
    var startDate: String,                         // Start date for the task
    var minTargetHours: Int,                       // Minimum target hours for the task
    var maxTargetHours: Int,                       // Maximum target hours for the task
    var isStarted: Boolean = false,                // Flag to track if the task is started
    var isArchived: Boolean = false,               // Flag to track if the task is archived
    val sessionHistory: MutableList<TaskSession> = mutableListOf() // List of session histories

){
    fun getTotalSessionDuration(): String {
        var totalMinutes = 0
        for (session in sessionHistory) {
            session.sessionDuration?.let {
                val parts = it.split(":")
                if (parts.size == 2) {
                    val hours = parts[0].toIntOrNull() ?: 0
                    val minutes = parts[1].toIntOrNull() ?: 0
                    totalMinutes += hours * 60 + minutes
                }
            }
        }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format("%d:%02d", hours, minutes)
    }
}

// Data class for each session of a task
data class TaskSession(
    var id: String = UUID.randomUUID().toString(), // Unique identifier for the session
    var sessionDescription: String = "",           // Date when the session started
    var sessionStartDate: String = "",             // Description of the session
    val startTime: String,                         // Time when the session started
    var endTime: String? = null,                   // Time when the session ended
    var sessionDuration: String? = null,           // Calculated duration of the session
    var imagePath: String? = null                  // Path to the image captured during the session

)