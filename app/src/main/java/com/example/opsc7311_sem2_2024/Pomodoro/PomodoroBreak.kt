package com.example.opsc7311_sem2_2024.Pomodoro

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class PomodoroBreak(
    var breakId: String = "",
    var taskId: String? = null,
    var sessionId: String? = null,
    var reason: String = "",
    var date: String = "",
    var startTime: String = "",
    var endTime: String? = null,
    var duration: String? = null,
    var imagePath: String? = null
)
