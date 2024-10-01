// TaskManager.kt
import com.example.opsc7311_sem2_2024.TaskItem
import com.example.opsc7311_sem2_2024.TaskSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskManager {

    private val taskList = mutableListOf<TaskItem>()

    // Add a new task
    fun addTask(
        title: String,
        category: String,
        time: String,
        creationDate: String,
        creationTime: String,
        startDate: String,
        minTargetHours: Int,
        maxTargetHours: Int,
        isStarted: Boolean = false
    ): TaskItem {
        val task = TaskItem(
            title = title,
            category = category,
            time = time,
            creationDate = creationDate,
            creationTime = creationTime,
            startDate = startDate,
            minTargetHours = minTargetHours,
            maxTargetHours = maxTargetHours,
            isStarted = isStarted
        )
        taskList.add(task)
        return task
    }

    // Get all tasks
    fun getTasks(): List<TaskItem> {
        return taskList
    }
}
