package com.example.opsc7311_sem2_2024.Notes

data class Note(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var isCompleted: Boolean = false,
    var importance: String = "Medium" // Default to Medium importance
)

