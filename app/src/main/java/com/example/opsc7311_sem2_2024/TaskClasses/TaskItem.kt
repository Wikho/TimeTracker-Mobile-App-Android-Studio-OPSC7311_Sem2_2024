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
)

// Data class for each session of a task
data class TaskSession(
    var sessionDescription: String = "",
    var sessionStartDate: String = "",
    var startTime: String = "",
    var endTime: String? = null,
    var sessionDuration: String? = null,
    var imagePath: String? = null
)
