package com.example.opsc7311_sem2_2024

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    fun insertTask(task: TaskItem) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun updateTask(task: TaskItem) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun deleteTask(task: TaskItem) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    fun getTaskById(taskId: String, callback: (TaskItem?) -> Unit) = viewModelScope.launch {
        val task = repository.getTaskById(taskId)
        callback(task)
    }

    fun getAllTasks(callback: (List<TaskItem>) -> Unit) = viewModelScope.launch {
        val tasks = repository.getAllTasks()
        callback(tasks)
    }
}