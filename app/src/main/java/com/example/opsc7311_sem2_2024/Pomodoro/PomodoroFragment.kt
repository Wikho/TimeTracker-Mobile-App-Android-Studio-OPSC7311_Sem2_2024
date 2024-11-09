package com.example.opsc7311_sem2_2024.Pomodoro

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.opsc7311_sem2_2024.R

class PomodoroFragment : Fragment() {
    // Declaring Variables
    private lateinit var pomodoroValueTextView: TextView
    private lateinit var submitPomodoroMinutes: EditText
    private lateinit var startPomodoroBtn: ImageButton
    private lateinit var pausePomodoroBtn: ImageButton
    private lateinit var cancelPomodoroBtn: ImageButton

    private var countDownTimer: CountDownTimer? = null
    private var remainingTimeInMillis: Long = 0
    private var isPomodoroRunning = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pomodoro, container, false)

        // Initialize UI Components
        pomodoroValueTextView = view.findViewById(R.id.TimerValueTxt)
        submitPomodoroMinutes = view.findViewById(R.id.TimerMinuteValueTxt)
        startPomodoroBtn = view.findViewById(R.id.PlayPTimerBtn)
        pausePomodoroBtn = view.findViewById(R.id.PausePTimerBtn)
        cancelPomodoroBtn = view.findViewById(R.id.CancelPTimerBtn)

        // Setting The Button Click Listeners & Input Handlers
        startPomodoroBtn.setOnClickListener {
            if (remainingTimeInMillis == 0L) {
                val input = submitPomodoroMinutes.text.toString()

                // Convert The Input Time Value To Milliseconds
                if (input.isNotEmpty()) {
                    val minutes = input.toLong()
                    remainingTimeInMillis = minutes * 60 * 1000
                    startTimer(remainingTimeInMillis)
                } else {
                    Toast.makeText(context, "Please Fill The Time Input Field", Toast.LENGTH_SHORT).show()
                }
            } else {
                resumeTimer()
            }
        }

        pausePomodoroBtn.setOnClickListener {
            pauseTimer()
        }

        cancelPomodoroBtn.setOnClickListener {
            cancelTimer()
        }

        updateButtonConditions()

        return view
    }

    // Handle The Actions For When The User Presses The Start Button (Start Condition)
    private fun startTimer(millisInFuture: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(millisInFuture, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeInMillis = millisUntilFinished

                // Convert The Milliseconds Value To Other Time Values
                val hours = (millisUntilFinished / 1000) / 3600
                val minutes = (millisUntilFinished / 1000 % 3600) / 60
                val seconds = (millisUntilFinished / 1000 % 60)

                pomodoroValueTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                pomodoroValueTextView.text = "00:00:00"
                isPomodoroRunning = false
                updateButtonConditions()
            }
        }.start()

        isPomodoroRunning = true
        updateButtonConditions()
    }

    // Handle The Actions For When The User Presses The Pause Button
    private fun pauseTimer() {
        countDownTimer?.cancel()
        isPomodoroRunning = false
        updateButtonConditions()
    }

    // Handles The Action For When The User Presses The Start Button (Resume Condition)
    private fun resumeTimer(){
        startTimer(remainingTimeInMillis)
    }

    // Handle The Actions For When The User Presses The Cancel Button
    @SuppressLint("SetTextI18n")
    private fun cancelTimer() {
        countDownTimer?.cancel()
        remainingTimeInMillis = 0
        pomodoroValueTextView.text = "00:00:00"
        isPomodoroRunning = false
        updateButtonConditions()
    }

    // Update The Conditions Of The UI Components To Match The Condition Of The Timer
    private fun updateButtonConditions() {
        if (isPomodoroRunning) {
            submitPomodoroMinutes.visibility = TextView.GONE
            pausePomodoroBtn.visibility = Button.VISIBLE
            cancelPomodoroBtn.visibility = Button.VISIBLE
            startPomodoroBtn.visibility = Button.GONE
        } else if (remainingTimeInMillis > 0) {
            submitPomodoroMinutes.visibility = TextView.GONE
            startPomodoroBtn.visibility = Button.VISIBLE
            cancelPomodoroBtn.visibility = Button.VISIBLE
            pausePomodoroBtn.visibility = Button.GONE
        } else {
            submitPomodoroMinutes.visibility = TextView.VISIBLE
            startPomodoroBtn.visibility = Button.VISIBLE
            cancelPomodoroBtn.visibility = Button.GONE
            pausePomodoroBtn.visibility = Button.GONE
        }
    }
}