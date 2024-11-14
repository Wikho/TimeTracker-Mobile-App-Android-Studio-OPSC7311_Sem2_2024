// SessionStartedActivity.kt
package com.example.opsc7311_sem2_2024.Session

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.MainScreen
import com.example.opsc7311_sem2_2024.Notes.NotesActivity
import com.example.opsc7311_sem2_2024.Notes.NotesFragment
import com.example.opsc7311_sem2_2024.Pomodoro.PomodoroActivity
import com.example.opsc7311_sem2_2024.Pomodoro.PomodoroFragment
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

    private var totalBreakTime = 0L
    private var breakCount = 0

    private var timeElapsed: Long = 0L
    private var sessionStartTime: Long = 0L

    private var timer: CountDownTimer? = null

    private var breakReminderInterval: Long = 0L


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



        // Button listeners
        btnBreak.setOnClickListener {
            showBreakPrompt()
        }

        btnStop.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Stop Session")
            builder.setMessage("Are you sure you want to stop the session?")
            builder.setPositiveButton("Yes") { _, _ ->
                stopSession()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        }

        btnNotes.setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java)
            intent.putExtra("isSessionMode", true)
            intent.putExtra("taskId", taskId)
            startActivity(intent)
        }

        // Get break reminder interval from settings
        val breakReminderSetting = SettingsSingleton.getSettingValue("Break Reminder") as? String ?: "Off"
        breakReminderInterval = when (breakReminderSetting) {
            "10" -> 10 * 60 * 1000L
            "15" -> 15 * 60 * 1000L
            "25" -> 25 * 60 * 1000L
            "30" -> 30 * 60 * 1000L
            "45" -> 45 * 60 * 1000L
            "60" -> 60 * 60 * 1000L
            else -> 0L
        }

        // Load task and session
        loadTaskAndSession()


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
        val workedMinutes = task?.getTotalWorkedMinutesToday() ?: 0
        val timeLeftMinutes = minGoalMinutes - workedMinutes

        if (timeLeftMinutes > 0) {
            tvTimeLeft.text = "You have $timeLeftMinutes minutes left to reach your minimum goal."
        } else {
            tvTimeLeft.text = "You have reached your minimum goal!"
        }
    }

    private fun startTimer() {
        sessionStartTime = System.currentTimeMillis() - timeElapsed
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeElapsed = System.currentTimeMillis() - sessionStartTime
                updateTimerUI()

                if (breakReminderInterval > 0 && timeElapsed >= breakReminderInterval) {
                    // Reset timer
                    sessionStartTime = System.currentTimeMillis()
                    showBreakReminder()
                }
            }

            override fun onFinish() {
                // Not used since we're counting up
            }
        }.start()
    }

    private fun showBreakReminder() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Break Reminder")
        builder.setMessage("Do you want to take a break?")
        builder.setPositiveButton("Yes") { _, _ ->
            openPomodoroFragment()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun updateTimerUI() {
        val totalSeconds = timeElapsed / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        tvTaskTime.text = timeFormatted

        // Calculate time left to reach minimum goal
        val minGoalMinutes = task?.minTargetHours?.times(60) ?: 0
        val workedMinutes = task?.getTotalWorkedMinutesToday() ?: 0
        val sessionMinutes = (timeElapsed / 60000).toInt()
        val totalWorkedMinutes = workedMinutes + sessionMinutes

        val timeLeftMinutes = minGoalMinutes - totalWorkedMinutes

        if (timeLeftMinutes > 0) {
            tvTimeLeft.text = "You have $timeLeftMinutes minutes left to reach your minimum goal."
        } else {
            tvTimeLeft.text = "You have reached your minimum goal!"
        }
    }

    private fun showBreakPrompt() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Take a Break")
        builder.setMessage("Do you want to take a break?")
        builder.setPositiveButton("Yes") { _, _ ->
            openPomodoroFragment()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun openPomodoroFragment() {
        val intent = Intent(this, PomodoroActivity::class.java)
        intent.putExtra("taskId", taskId)
        intent.putExtra("sessionId", sessionId)
        intent.putExtra("isTaskStarted", true)
        breakLauncher.launch(intent)
    }

    private val breakLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val breakDuration = result.data?.getLongExtra("breakDuration", 0L) ?: 0L
            totalBreakTime += breakDuration
            breakCount += 1
        }
    }

    private fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun stopSession() {
        val currentTime = System.currentTimeMillis()
        val endTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))

        session?.let { currentSession ->
            currentSession.endTime = endTime
            currentSession.sessionDuration = calculateDuration(currentSession.startTime, endTime)
            currentSession.breakCount = breakCount
            currentSession.totalBreakTime = totalBreakTime // Assign the Long value directly

            // **Set the correct taskId here**
            currentSession.taskId = task?.id ?: ""

            task?.isStarted = false

            // Update the session in the task's sessionHistory
            task?.sessionHistory?.find { it.sessionId == currentSession.sessionId }?.let {
                val index = task?.sessionHistory?.indexOf(it)
                if (index != null && index >= 0) {
                    task?.sessionHistory?.set(index, currentSession)
                }
            }

            // Update the task in Firebase
            task?.let { taskItem ->
                firebaseManager.updateTask(taskItem) { success, message ->
                    if (success) {
                        // Update category stats
                        val durationInSeconds = calculateDurationInSeconds(currentSession.sessionDuration)
                        val categories = taskItem.category.split(",").map { it.trim().uppercase() }
                        firebaseManager.updateCategoryStats(categories, durationInSeconds)
                        showSessionSummaryDialog(currentSession)
                    } else {
                        // Handle error
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
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
        message.append("Total Break Time: ${formatDuration(session.totalBreakTime)}\n")

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
                // Navigate to the main activity or refresh the fragment
                val intent = Intent(this, MainScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
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

    private fun calculateDurationInSeconds(duration: String?): Long {
        duration?.let {
            val parts = it.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                val seconds = parts[2].toIntOrNull() ?: 0
                return (hours * 3600 + minutes * 60 + seconds).toLong()
            }
        }
        return 0L
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        startTimer()
    }

}
