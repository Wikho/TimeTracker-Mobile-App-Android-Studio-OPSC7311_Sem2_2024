package com.example.opsc7311_sem2_2024.TaskClasses

import androidx.room.*
import com.example.opsc7311_sem2_2024.CategoryStats

@Dao
interface TaskItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM task_items WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("SELECT * FROM task_items WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskItem?

    @Query("SELECT * FROM task_items")
    suspend fun getAllTasks(): List<TaskItem>

    @Query("SELECT * FROM task_items WHERE isArchived = 1")
    suspend fun getArchivedTasks(): List<TaskItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCategoryStats(categoryStats: CategoryStats)

    @Query("SELECT * FROM category_stats WHERE categoryName = :categoryName")
    suspend fun getCategoryStats(categoryName: String): CategoryStats?

}