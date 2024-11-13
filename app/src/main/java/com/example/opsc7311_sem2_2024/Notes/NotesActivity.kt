package com.example.opsc7311_sem2_2024.Notes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7311_sem2_2024.R

class NotesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to a layout that contains a FrameLayout
        setContentView(R.layout.activity_notes)

        val isSessionMode = intent.getBooleanExtra("isSessionMode", false)
        val taskId = intent.getStringExtra("taskId")

        val notesFragment = NotesFragment()
        val bundle = Bundle()
        bundle.putBoolean("isSessionMode", isSessionMode)
        bundle.putString("taskId", taskId)
        notesFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.notes_fragment_container, notesFragment)
            .commit()
    }
}
