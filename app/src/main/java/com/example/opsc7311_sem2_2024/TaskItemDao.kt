package com.example.opsc7311_sem2_2024

import androidx.room.*

@Dao
interface TaskItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("SELECT * FROM task_items WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskItem?

    @Query("SELECT * FROM task_items")
    suspend fun getAllTasks(): List<TaskItem>

}