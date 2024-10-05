package com.example.opsc7311_sem2_2024

class TaskRepository(private val taskItemDao: TaskItemDao) {

    suspend fun insertTask(task: TaskItem) {
        taskItemDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskItem) {
        taskItemDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskItem) {
        taskItemDao.deleteTask(task)
    }

    suspend fun getTaskById(taskId: String): TaskItem? {
        return taskItemDao.getTaskById(taskId)
    }

    suspend fun getAllTasks(): List<TaskItem> {
        return taskItemDao.getAllTasks()
    }

}