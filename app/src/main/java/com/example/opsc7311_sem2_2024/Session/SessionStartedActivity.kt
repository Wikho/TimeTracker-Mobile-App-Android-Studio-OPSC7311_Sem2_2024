// SessionStartedActivity.kt
package com.example.opsc7311_sem2_2024.Session

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.TaskClasses.TaskItem
import com.example.opsc7311_sem2_2024.TaskClasses.TaskSession
import java.text.SimpleDateFormat
import java.util.*

class SessionStartedActivity : AppCompatActivity() {

    private lateinit var tvTaskName: TextView
    private lateinit var tvTaskTime: TextView
    private lateinit var tvTimeLeft: TextView
    private lateinit var btnBreak: Button
    private lateinit var btnStop: Button
    private lateinit var btnNotes: Button

    private val firebaseManager = FirebaseManager()
    private var taskId: String? = null
    private var sessionId: String? = null
    private var task: TaskItem? = null
    private var session: TaskSession? = null

    private var timer: CountDownTimer? = null
    private var isBreak = false
    private var totalBreakTime = 0L
    private var breakCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set your layout here
        setContentView(R.layout.activity_session_started)

        // Initialize views
        tvTaskName = findViewById(R.id.tvTaskName)
        tvTaskTime = findViewById(R.id.tvTaskTime)
        tvTimeLeft = findViewById(R.id.tvTimeLeft)
        btnBreak = findViewById(R.id.btnBreak)
        btnStop = findViewById(R.id.btnStop)
        btnNotes = findViewById(R.id.btnNotes)

        // Get taskId and sessionId from intent
        taskId = intent.getStringExtra("taskId")
        sessionId = intent.getStringExtra("sessionId")

        // Load task and session
        loadTaskAndSession()

        // Button listeners
        btnBreak.setOnClickListener {
            if (isBreak) {
                endBreak()
            } else {
                startBreak()
            }
        }

        btnStop.setOnClickListener {
            stopSession()
        }

        btnNotes.setOnClickListener {
            // Open Notes activity or fragment
        }
    }

    private fun loadTaskAndSession() {
        taskId?.let { id ->
            firebaseManager.getTaskById(id) { fetchedTask ->
                task = fetchedTask
                // Find the session in the list by sessionId
                session = task?.sessionHistory?.find { it.sessionId == sessionId }
                if (task != null && session != null) {
                    setupUI()
                    startTimer()
                } else {
                    finish() // End activity if task or session is not found
                }
            }
        }
    }

    private fun setupUI() {
        tvTaskName.text = task?.title

        // Calculate total task time and worked time
        val taskTime = task?.time ?: "00:00"
        val totalWorkedTime = task?.getTotalSessionDuration() ?: "00:00"

        tvTaskTime.text = "$taskTime / $totalWorkedTime"

        // Time left to reach min goal
        val minGoalMinutes = task?.minTargetHours?.times(60) ?: 0
        val workedMinutes = task?.getTotalWorkedMinutes() ?: 0
        val timeLeftMinutes = minGoalMinutes - workedMinutes

        if (timeLeftMinutes > 0) {
            tvTimeLeft.text = "You have $timeLeftMinutes minutes left to reach your minimum goal."
        } else {
            tvTimeLeft.text = "You have reached your minimum goal!"
        }
    }

    private fun startTimer() {
        // Implement timer logic if needed
    }

    private fun startBreak() {
        isBreak = true
        breakCount++
        btnBreak.text = "End Break"
        // Pause timer if you have one
    }

    private fun endBreak() {
        isBreak = false
        btnBreak.text = "Break"
        // Resume timer if you have one
    }

    private fun stopSession() {
        val currentTime = System.currentTimeMillis()
        val endTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))

        session?.let {
            it.endTime = endTime
            it.sessionDuration = calculateDuration(it.startTime, endTime)
            it.breakCount = breakCount
            it.totalBreakTime = totalBreakTime

            task?.isStarted = false

            // Update the task in Firebase
            task?.let { taskItem ->
                firebaseManager.updateTask(taskItem) { success, message ->
                    if (success) {
                        showSessionSummaryDialog(it)
                    } else {
                        finish()
                    }
                }
            }
        }
    }

    private fun showSessionSummaryDialog(session: TaskSession) {
        val message = StringBuilder()
        message.append("Session Duration: ${session.sessionDuration}\n")
        message.append("Breaks Taken: ${session.breakCount}\n")
        message.append("Total Break Time: ${session.totalBreakTime} seconds\n")

        // Check if min or max goals are achieved
        task?.let { taskItem ->
            val sessionDurationMinutes = calculateDurationInMinutes(session.sessionDuration)
            val minTargetMinutes = taskItem.minTargetHours * 60
            val maxTargetMinutes = taskItem.maxTargetHours * 60

            if (sessionDurationMinutes >= minTargetMinutes) {
                message.append("You achieved your minimum goal!\n")
            } else {
                message.append("You did not reach your minimum goal.\n")
            }

            if (sessionDurationMinutes > maxTargetMinutes) {
                message.append("You exceeded your maximum goal.")
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Session Summary")
            .setMessage(message.toString())
            .setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.dismiss()
                finish()
            }
            .create()

        dialog.show()
    }

    private fun calculateDuration(startTime: String, endTime: String): String {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val startDate = format.parse(startTime)
        val endDate = format.parse(endTime)
        val difference = endDate.time - startDate.time
        val seconds = (difference / 1000) % 60
        val minutes = (difference / (1000 * 60)) % 60
        val hours = (difference / (1000 * 60 * 60))
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun calculateDurationInMinutes(duration: String?): Int {
        duration?.let {
            val parts = it.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                return hours * 60 + minutes
            }
        }
        return 0
    }
}
