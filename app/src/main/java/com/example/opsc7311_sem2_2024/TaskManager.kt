package com.example.opsc7311_sem2_2024

import android.icu.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskManager {

    // A list to hold all tasks
    private val taskList: MutableList<TaskItem> = mutableListOf()

    // Function to add a new task
    fun addTask(title: String, tag: String, minTargetHours: Int, maxTargetHours: Int): TaskItem {
        val newTask = TaskItem(
            title = title,
            tag = tag,
            time = "00:00:00",
            creationDate = getCurrentDate(),
            creationTime = getCurrentTime(),
            minTargetHours = minTargetHours,
            maxTargetHours = maxTargetHours
        )
        taskList.add(newTask)
        return newTask
    }

    // Function to get a task by its ID
    fun getTaskById(taskId: String): TaskItem? {
        return taskList.find { it.id == taskId }
    }

    // Function to edit a task's details
    fun editTask(taskId: String, newTitle: String, newTag: String, newMinTargetHours: Int, newMaxTargetHours: Int): Boolean {
        val task = getTaskById(taskId)
        task?.let {
            it.title = newTitle
            it.tag = newTag
            it.minTargetHours = newMinTargetHours
            it.maxTargetHours = newMaxTargetHours
            return true
        }
        return false // Task not found
    }

    // Function to start a session for a specific task
    fun startSession(taskId: String): Boolean {
        val task = getTaskById(taskId)
        task?.let {
            if (!it.isStarted) {
                startSession(it)
                return true
            }
        }
        return false // Task not found or already started
    }

    // Function to end a session for a specific task, also requires the path to the session image
    fun endSession(taskId: String, imagePath: String): Boolean {
        val task = getTaskById(taskId)
        task?.let {
            if (it.isStarted) {
                endSession(it, imagePath)
                return true
            }
        }
        return false // Task not found or not started
    }

    // Function to get all tasks
    fun getAllTasks(): List<TaskItem> {
        return taskList
    }

    // Function to remove a task by its ID
    fun removeTask(taskId: String): Boolean {
        val task = getTaskById(taskId)
        task?.let {
            taskList.remove(it)
            return true
        }
        return false // Task not found
    }

    // Function to retrieve the session history of a task
    fun getTaskHistory(taskId: String): List<TaskSession>? {
        return getTaskById(taskId)?.sessionHistory
    }

    // Utility functions
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun calculateDuration(startTime: String, endTime: String): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val start = sdf.parse(startTime)
        val end = sdf.parse(endTime)
        val durationMillis = end.time - start.time
        val seconds = (durationMillis / 1000) % 60
        val minutes = (durationMillis / (1000 * 60)) % 60
        val hours = (durationMillis / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Private functions for handling task sessions
    private fun startSession(task: TaskItem) {
        task.isStarted = true
        val currentTime = getCurrentTime()
        task.sessionHistory.add(TaskSession(startTime = currentTime))
    }

    private fun endSession(task: TaskItem, imagePath: String) {
        task.isStarted = false
        val currentTime = getCurrentTime()
        task.sessionHistory.lastOrNull()?.let { session ->
            session.endTime = currentTime
            session.sessionDuration = calculateDuration(session.startTime, session.endTime)
            session.imagePath = imagePath
        }
    }
}