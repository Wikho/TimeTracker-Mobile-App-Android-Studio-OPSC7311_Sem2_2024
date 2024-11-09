package com.example.opsc7311_sem2_2024.TaskClasses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    fun insertTask(task: TaskItem) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun updateTask(task: TaskItem, callback: () -> Unit) = viewModelScope.launch {
        repository.updateTask(task)
        withContext(Dispatchers.Main) {
            callback()
        }
    }

    fun deleteTask(taskId: String) = viewModelScope.launch {
        repository.deleteTask(taskId)
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