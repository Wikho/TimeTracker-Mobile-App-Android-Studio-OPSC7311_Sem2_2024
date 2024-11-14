package com.example.opsc7311_sem2_2024.Pomodoro


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.opsc7311_sem2_2024.BreakInfo.BreakInfoFragment
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PomodoroFragment : Fragment() {

    private lateinit var playButton: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var cancelButton: ImageButton
    private lateinit var timerValueText: TextView
    private lateinit var timerMinuteValueEditText: EditText
    private lateinit var tvTaskTitle: TextView
    private lateinit var backButton: ImageButton
    private lateinit var breakInfoButton: Button

    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0

    private var pomodoroBreak: PomodoroBreak? = null

    private var taskId: String? = null
    private var isTaskStarted: Boolean = false

    private val firebaseManager = FirebaseManager()
    private var selectedImageUri: Uri? = null
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private var imageUri: Uri? = null

    private var currentPhotoPath: String? = null

    private var currentSessionId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskId = it.getString("taskId")
            isTaskStarted = it.getBoolean("isTaskStarted", false)
            currentSessionId = it.getString("sessionId")
        }

        // Initialize the image picker launcher
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                selectedImageUri = imageUri
            } else {
                Toast.makeText(requireContext(), "Failed to capture image.", Toast.LENGTH_SHORT).show()
            }
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                capturePhoto()
            } else {
                Toast.makeText(requireContext(), "Camera permission is needed to take a photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pomodoro, container, false)

        playButton = view.findViewById(R.id.PlayPTimerBtn)
        pauseButton = view.findViewById(R.id.PausePTimerBtn)
        cancelButton = view.findViewById(R.id.CancelPTimerBtn)
        timerValueText = view.findViewById(R.id.TimerValueTxt)
        timerMinuteValueEditText = view.findViewById(R.id.TimerMinuteValueTxt)
        tvTaskTitle = view.findViewById(R.id.tvTaskTitle)
        backButton = view.findViewById(R.id.btnBack)
        breakInfoButton = view.findViewById(R.id.btnBreakInfo)

        if (isTaskStarted && taskId != null) {
            setupForTask()
        } else {
            setupForGeneralUse()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackOrCancel()
        }

        cancelButton.setOnClickListener {

            val endTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val startTime = pomodoroBreak?.startTime ?: endTime
            val duration = calculateDuration(startTime, endTime)

            AlertDialog.Builder(requireContext())
                .setTitle("End Break")
                .setMessage("Are you sure you want to end the break? Break duration: $duration")
                .setPositiveButton("Yes") { dialog, which ->
                    endBreak()
                }
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                    Log.d("PomodoroFragment", "Break end canceled by user.")
                }
                .create()
                .show()
        }

        breakInfoButton.setOnClickListener {
            val breakInfoFragment = BreakInfoFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, breakInfoFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun setupForTask() {

        // Display the task title
        tvTaskTitle.visibility = View.VISIBLE
        backButton.visibility = View.VISIBLE
        breakInfoButton.visibility = View.GONE

        firebaseManager.getTaskById(taskId!!) { task ->
            task?.let {
                tvTaskTitle.text = "Break for Task: ${it.title}"
            }
        }

        // Adjust buttons and interactions
        playButton.setOnClickListener {
            if (isTimerRunning) {
                // Timer is already running, do nothing
            } else if (timeLeftInMillis != 0L) {
                // Resume the timer
                if (timeLeftInMillis > 0L) {
                    startTimer()
                } else {
                    // Resume negative timer
                    startNegativeTimer()
                }
                playButton.visibility = View.GONE
                pauseButton.visibility = View.VISIBLE
            } else {
                // Start a new timer
                promptForReasonAndStartTimer()
            }
        }

        pauseButton.setOnClickListener {
            pauseTimer()
        }

        // Show the back button
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener {

            if (!isTimerRunning) {
                //No session started, navigate back
                requireActivity().finish()
            }

            val endTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val startTime = pomodoroBreak?.startTime ?: endTime
            val duration = calculateDuration(startTime, endTime)

            AlertDialog.Builder(requireContext())
                .setTitle("End Break")
                .setMessage("Are you sure you want to end the break? Break duration: $duration")
                .setPositiveButton("Yes") { dialog, which ->
                    endBreak()
                }
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                    Log.d("PomodoroFragment", "Break end canceled by user.")
                }
                .create()
                .show()
        }
    }

    private fun setupForGeneralUse() {
        // Hide task title
        tvTaskTitle.visibility = View.GONE
        backButton.visibility = View.GONE
        breakInfoButton.visibility = View.VISIBLE

        // Regular setup
        playButton.setOnClickListener {
            if (isTimerRunning) {
                // Timer is already running, do nothing
            } else if (timeLeftInMillis != 0L) {
                // Resume the timer
                if (timeLeftInMillis > 0L) {
                    startTimer()
                } else {
                    // Resume negative timer
                    startNegativeTimer()
                }
                playButton.visibility = View.GONE
                pauseButton.visibility = View.VISIBLE
            } else {
                // Start a new timer
                promptForReasonAndStartTimer()
            }
        }

        pauseButton.setOnClickListener {
            pauseTimer()
        }


    }

    private fun promptForReasonAndStartTimer() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pomodoro_reason, null)
        val etReason = dialogView.findViewById<EditText>(R.id.etPomodoroReason)
        val btnSelectPhoto = dialogView.findViewById<Button>(R.id.btnSelectPomodoroPhoto)

        btnSelectPhoto.setOnClickListener {
            // Open camera to take a photo
            capturePhoto()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Start Break")
            .setView(dialogView)
            .setPositiveButton("Start") { _, _ ->
                val reason = etReason.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(requireContext(), "Reason cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                startTimerWithReason(reason)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }

    private fun capturePhoto() {
        val photoFile = createImageFile()
        photoFile?.let {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                it
            )
            imageUri = uri // Store it for later use
            takePhotoLauncher.launch(uri)
        }
    }

    private fun createImageFile(): File? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            val file = File.createTempFile(
                "JPEG_${timestamp}_",
                ".jpg",
                storageDir
            )
            currentPhotoPath = file.absolutePath
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun startTimerWithReason(reason: String) {
        val minutesInput = timerMinuteValueEditText.text.toString().toIntOrNull()
        if (minutesInput == null || minutesInput <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid number of minutes", Toast.LENGTH_SHORT).show()
            return
        }

        timeLeftInMillis = minutesInput * 60 * 1000L
        startTimer()

        // Hide start button and show "End Break" button
        playButton.visibility = View.GONE
        pauseButton.visibility = View.VISIBLE
        cancelButton.visibility = View.VISIBLE

        // Store the break data
        val currentTime = System.currentTimeMillis()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(currentTime))
        val startTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))

        pomodoroBreak = PomodoroBreak(
            breakId = UUID.randomUUID().toString(),
            taskId = if (isTaskStarted) taskId else null,
            sessionId = if (isTaskStarted) currentSessionId else null, // Set sessionId
            reason = reason,
            date = date,
            startTime = startTime,
            endTime = null, // Initialize as null
            duration = null, // Initialize as null
            imagePath = null // Initialize as null
        )

        // Store the break data in Firebase
        pomodoroBreak?.let { currentBreak ->
            firebaseManager.savePomodoroBreak(currentBreak, selectedImageUri) { success, message ->
                if (success) {
                    Log.d("PomodoroFragment", "Break started and saved successfully: $currentBreak")
                } else {
                    Toast.makeText(requireContext(), "Error saving break: $message", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimer()
            }

            override fun onFinish() {
                timerValueText.setTextColor(Color.RED)
                timerValueText.text = "00:00"
                isTimerRunning = false

                // Continue counting into negatives
                startNegativeTimer()
            }
        }.start()
        isTimerRunning = true

        // Adjust button visibility
        playButton.visibility = View.GONE
        pauseButton.visibility = View.VISIBLE
    }

    private fun startNegativeTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis -= 1000
                updateTimer()
            }

            override fun onFinish() {
                // Not used
            }
        }.start()
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false

        // Show the Play button to resume
        playButton.visibility = View.VISIBLE
        pauseButton.visibility = View.GONE
    }

    private fun isTimerStarted(): Boolean {
        return isTimerRunning && pomodoroBreak != null
    }

    private fun endBreak() {
        timer?.cancel()
        isTimerRunning = false
        timeLeftInMillis = 0L

        // Calculate actual break duration
        val endTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val startTime = pomodoroBreak?.startTime ?: endTime
        val duration = calculateDuration(startTime, endTime)

        pomodoroBreak?.let { currentBreak ->
            // Update endTime and duration
            currentBreak.endTime = endTime
            currentBreak.duration = duration

            // Update only endTime and duration in Firebase
            firebaseManager.updateBreakEndTimeAndDuration(
                breakId = currentBreak.breakId,
                taskId = currentBreak.taskId,
                endTime = endTime,
                duration = duration
            ) { success, message ->
                if (!success) {
                    Toast.makeText(requireContext(), "Error updating break: $message", Toast.LENGTH_SHORT).show()
                } else {
                    // Update category stats if associated with a task
                    if (!currentBreak.taskId.isNullOrEmpty()) {
                        firebaseManager.getTaskById(currentBreak.taskId!!) { taskItem ->
                            taskItem?.let { task ->
                                val durationInSeconds = calculateDurationInSeconds(duration)
                                val categories = task.category.split(",").map { it.trim().uppercase() }
                                firebaseManager.updateCategoryStats(categories, durationInSeconds)
                            }
                        }
                    }

                    // Reset UI
                    resetTimerUI()

                    if (isTaskStarted && taskId != null) {
                        val durationInSeconds = calculateDurationInSeconds(duration)
                        val resultIntent = Intent().apply {
                            putExtra("breakDuration", durationInSeconds)
                        }
                        requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                        requireActivity().finish()
                    }

                }
            }
        } ?: run {
            // If pomodoroBreak is null, just reset UI and finish
            resetTimerUI()
        }
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

    private fun updateTimer() {
        val totalSeconds = timeLeftInMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val timeFormatted = String.format("%02d:%02d", Math.abs(minutes), Math.abs(seconds))

        if (timeLeftInMillis < 0) {
            timerValueText.setTextColor(Color.RED)
            timerValueText.text = "-$timeFormatted"
        } else {
            timerValueText.setTextColor(Color.BLACK)
            timerValueText.text = timeFormatted
        }
    }

    private fun handleBackOrCancel() {
        timer?.cancel()
        isTimerRunning = false

        val endTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val startTime = pomodoroBreak?.startTime ?: endTime
        val duration = calculateDuration(startTime, endTime)
        val durationInSeconds = calculateDurationInSeconds(duration)

        val message = "Your break duration was: $duration"

        AlertDialog.Builder(requireContext())
            .setTitle("Break Ended")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                if (isTaskStarted) {
                    val resultIntent = Intent().apply {
                        putExtra("breakDuration", durationInSeconds)
                    }
                    requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                    requireActivity().finish()
                } else {
                    // Reset UI for normal mode
                    resetTimerUI()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun resetTimerUI() {
        timeLeftInMillis = 0L
        timerValueText.setTextColor(Color.BLACK)
        timerValueText.text = "00:00:00"
        playButton.visibility = View.VISIBLE
        pauseButton.visibility = View.GONE
        cancelButton.visibility = View.GONE
    }
}

