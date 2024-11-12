package com.example.opsc7311_sem2_2024.Pomodoro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7311_sem2_2024.R

class PomodoroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set your activity layout
        setContentView(R.layout.activity_pomodoro)

        if (savedInstanceState == null) {
            val fragment = PomodoroFragment()
            fragment.arguments = intent.extras

            supportFragmentManager.beginTransaction()
                .replace(R.id.pomodoro_fragment_container, fragment)
                .commit()
        }
    }
}